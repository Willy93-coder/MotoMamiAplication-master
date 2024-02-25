package com.motomami.Services.impl;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.motomami.Services.ProcessService;
import com.motomami.model.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.motomami.Utils.Constants.*;

@Service
public class ProcessServiceImpl implements ProcessService {
    @Value("${path.folder.outFiles}")
    String pathFolderoutFiles;
    @Value("${path.folder.inFiles}")
    String pathFolderinFiles;
    @Value("${path.folder.providers}")
    String pathFolderProviders;
    @Value("${path.folder.parts.providers}")
    String pathFolderPartsProviders;
    @Value("${path.folder.vehicles.providers}")
    String pathFolderVehiclesProviders;
    @Value("${path.folder.customers.providers}")
    String pathFolderCustomersProviders;
    @Value("${extension.file.providers}")
    String extensionFileProviders;
    @Value("url.db")
    String urlConnectionDB;
    Connection con;

    /**
     * Metodo que determina que fichero se leerá según el valor de source
     */
    public void readFileInfo(String source) {
        try {
            switch (source) {
                case C_SOURCE_PARTS:
                    readFileInfoParts();
                    break;
                case C_SOURCE_CUSTOMER:
                    readFileInfoCustomer();
                    break;
                case C_SOURCE_VEHICLE:
                    readFileInfoVehicles();
                    break;
                default:
            }
        } catch (Exception e) {
            System.err.println("Error leyendo los ficheros: " + e.getMessage());
        }
    }

    public void processInfoWithStatusNotProcessed(String source) {
        try {
            switch (source) {
                case C_SOURCE_PARTS:
                    processFileInfoParts();
                    break;
                case C_SOURCE_CUSTOMER:
                    processFileInfoCustomer();
                    break;
                case C_SOURCE_VEHICLE:
                    processFileInfoVehicles();
                    break;
                default:
            }
        } catch (Exception e) {
            System.err.println("Error procesando los datos de la interfaz: " + e.getMessage());
        }
    }

    private void processFileInfoVehicles() {
        if (!customersDNI.isEmpty()) {
            ArrayList<VehicleDto> vehicleDto;
            try {
                vehicleDto = getVehiclesInfoWithStatus(estadoFichero[0]);
                for (VehicleDto vehicle : vehicleDto) {
                    vehiclesId.add(vehicle.getInternalIdVehicle());
                    vehiclesIdExternal.add(vehicle.getIdVehicleExternal());
                }
            } catch (SQLException e) {
                System.err.println("Error al obtener los datos con status N  de la tabla mm_intvehicles: " + e.getMessage());
            }
        }
    }

    private void processFileInfoParts() {
        if (!customersDNI.isEmpty() && !vehiclesIdExternal.isEmpty()) {
            try {
                getPartInfoWithStatus(estadoFichero[0]);
            } catch (SQLException e) {
                System.err.println("Error al obtener los datos con status N  de la tabla mm_intparts: " + e.getMessage());
            }
        }
    }

    private ArrayList<PartDto> getPartInfoWithStatus(String pStatus) throws SQLException {
        con = DriverManager.getConnection("jdbc:mysql://localhost:3306/motomamidb", "root", "rootroot");
        GsonBuilder gsonBuilder = new GsonBuilder().serializeNulls().setPrettyPrinting();
        Gson gson = gsonBuilder.setPrettyPrinting().setDateFormat("yyyy/MM/dd").create();
        String query = "SELECT * FROM mm_intparts WHERE statusProcess = ?;";
        PreparedStatement ps = null;
        ResultSet rs = null;
        ArrayList<PartDto> parts = new ArrayList<>();
        try {
            ps = con.prepareStatement(query);
            ps.setString(1, pStatus);
            rs = ps.executeQuery();
            while (rs.next()) {
                PartDto part;
                String jsonPart = rs.getString("contJson");
                int id = rs.getInt("id");
                String operation = rs.getString("operation");
                part = gson.fromJson(jsonPart, PartDto.class);
                parts.add(part);
                if (operation.equals("New")) {
                    System.out.println("dentro del if de getPartInfoWithStatus");
                    try {
                        insertPart(jsonPart);
                        updateIntParts(id, "P", "", "");
                    } catch (Exception e) {
                        updateIntParts(id, "E", "MME", e.getMessage());
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al ejecutar la query en getPartInfoWithStatus: " + e.getMessage());
        } finally {
            if (rs != null) {
                rs.close();
            }
            if (ps != null) {
                ps.close();
            }
            if (con != null) {
                con.close();
            }
        }
        return parts;
    }

    private void insertPart(String jsonPart) throws SQLException {
        con = DriverManager.getConnection("jdbc:mysql://localhost:3306/motomamidb", "root", "rootroot");
        PreparedStatement ps = null;
        GsonBuilder gsonBuilder = new GsonBuilder().serializeNulls().setPrettyPrinting();
        Gson gson = gsonBuilder.setPrettyPrinting().setDateFormat("yyyy/MM/dd").create();
        PartDto partDto = gson.fromJson(jsonPart, PartDto.class);
        String query = "INSERT INTO mm_part " +
                "(datePartExternal, descriptionPartExternal, codeDamageExternal, codeDamage, identityCode, idExternal) " +
                "VALUES " +
                "(?, ?, ?, ?, ?, ?)";
        try {
            for (int i = 0; i < vehiclesIdExternal.size(); i++) {
                if (partDto.getIdExternal().equalsIgnoreCase(vehiclesIdExternal.get(i))) {
                    for (int j = 0; j < customersDNI.size(); j++) {
                        if (partDto.getIdentityCode().equalsIgnoreCase(customersDNI.get(j))) {
                            ps = con.prepareStatement(query);
                            ps.setDate(1, new java.sql.Date(partDto.getDatePartExternal().getTime()));
                            ps.setString(2, partDto.getDescriptionPartExternal());
                            ps.setString(3, partDto.getCodeDamageExternal());
                            ps.setString(4, partDto.getCodeDamage());
                            ps.setString(5, partDto.getIdentityCode());
                            ps.setString(6, partDto.getIdExternal());
                            int rs3 = ps.executeUpdate();
                            System.out.println("Se han actualizado " + rs3 + " registros");
                        }
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("No ha ido bien el insertado en mm_part: " + e.getMessage());
        } finally {
            if (ps != null) {
                ps.close();
            }
            if (con != null) {
                con.close();
            }
        }
    }

    private void updateIntParts(int id, String status, String codeError, String messageError) throws SQLException {
        con = DriverManager.getConnection("jdbc:mysql://localhost:3306/motomamidb", "root", "rootroot");
        String query = "UPDATE mm_intparts SET statusProcess = ?, msgError = ?, lastUpdate = sysdate(), codeError = ?, updatedBy = ? WHERE id = ?";
        PreparedStatement ps;
        try {
            ps = con.prepareStatement(query);
            ps.setString(1, status);
            ps.setString(2, messageError);
            ps.setString(3, codeError);
            ps.setString(4, user);
            ps.setInt(5, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error al hacer el update de mm_intparts: " + e.getMessage());
        }
    }

    private void processFileInfoCustomer() {
        ArrayList<CustomerDto> customerDto;
        try {
            customerDto = getCustomerInfoWithStatus(estadoFichero[0]);
            for (CustomerDto customer : customerDto) {
                customersDNI.add(customer.getDNI());
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener los datos con status N de la tabla mm_intcustomers: " + e.getMessage());
        }
    }

    private ArrayList<CustomerDto> getCustomerInfoWithStatus(String pStatus) throws SQLException {
        con = DriverManager.getConnection("jdbc:mysql://localhost:3306/motomamidb", "root", "rootroot");
        GsonBuilder gsonBuilder = new GsonBuilder().serializeNulls().setPrettyPrinting();
        Gson gson = gsonBuilder.setPrettyPrinting().setDateFormat("yyyy/MM/dd").create();
        String query = "SELECT * FROM mm_intcustomers WHERE statusProcess = ?;";
        PreparedStatement ps = null;
        ResultSet rs = null;
        ArrayList<CustomerDto> customers = new ArrayList<>();
        try {
            ps = con.prepareStatement(query);
            ps.setString(1, pStatus);
            rs = ps.executeQuery();
            while (rs.next()) {
                CustomerDto customer;
                String jsonCustomer = rs.getString("contJson");
                int id = rs.getInt("id");
                String operation = rs.getString("operation");
                customer = gson.fromJson(jsonCustomer, CustomerDto.class);
                customers.add(customer);
                if (operation.equals("New")) {
                    try {
                        insertCustomer(jsonCustomer);
                        updateIntCustomer(id, "P", "", "");
                    } catch (Exception e) {
                        updateIntCustomer(id, "E", "MME", e.getMessage());
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al ejecutar la query en getCustomerInfoWithStatus: " + e.getMessage());
        } finally {
            if (rs != null) {
                rs.close();
            }
            if (ps != null) {
                ps.close();
            }
            if (con != null) {
                con.close();
            }
        }
        return customers;
    }

    private void updateIntCustomer(int id, String status, String codeError, String messageError) throws SQLException {
        con = DriverManager.getConnection("jdbc:mysql://localhost:3306/motomamidb", "root", "rootroot");
        String query = "UPDATE mm_intcustomers SET statusProcess = ?, msgError = ?, lastUpdate = sysdate(), codeError = ?, updatedBy = ? WHERE id = ?";
        PreparedStatement ps;
        try {
            ps = con.prepareStatement(query);
            ps.setString(1, status);
            ps.setString(2, messageError);
            ps.setString(3, codeError);
            ps.setString(4, user);
            ps.setInt(5, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error al hacer el update de mm_intcustomers: " + e.getMessage());
        }
    }

    private ArrayList<VehicleDto> getVehiclesInfoWithStatus(String pStatus) throws SQLException {
        con = DriverManager.getConnection("jdbc:mysql://localhost:3306/motomamidb", "root", "rootroot");
        GsonBuilder gsonBuilder = new GsonBuilder().serializeNulls().setPrettyPrinting();
        Gson gson = gsonBuilder.setPrettyPrinting().setDateFormat("yyyy/MM/dd").create();
        String query = "SELECT * FROM mm_intvehicles WHERE statusProcess = ?;";
        PreparedStatement ps = null;
        ResultSet rs = null;
        ArrayList<VehicleDto> vehicles = new ArrayList<>();
        try {
            ps = con.prepareStatement(query);
            ps.setString(1, pStatus);
            rs = ps.executeQuery();
            while (rs.next()) {
                VehicleDto vehicle;
                String jsonVehicle = rs.getString("contJson");
                int id = rs.getInt("id");
                String operation = rs.getString("operation");
                vehicle = gson.fromJson(jsonVehicle, VehicleDto.class);
                vehicles.add(vehicle);
                if (operation.equals("New")) {
                    try {
                        insertVehicle(jsonVehicle);
                        updateIntVehicle(id, "P", "", "");
                    } catch (Exception e) {
                        updateIntVehicle(id, "E", "MME", e.getMessage());
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al ejecutar la query en getVehiclesInfoWithStatus: " + e.getMessage());
        } finally {
            if (rs != null) {
                rs.close();
            }
            if (ps != null) {
                ps.close();
            }
            if (con != null) {
                con.close();
            }
        }
        return vehicles;
    }

    private void insertVehicle(String jsonVehicle) throws SQLException {
        con = DriverManager.getConnection("jdbc:mysql://localhost:3306/motomamidb", "root", "rootroot");
        PreparedStatement ps = null;
        GsonBuilder gsonBuilder = new GsonBuilder().serializeNulls().setPrettyPrinting();
        Gson gson = gsonBuilder.setPrettyPrinting().setDateFormat("yyyy/MM/dd").create();
        VehicleDto vehicleDto = gson.fromJson(jsonVehicle, VehicleDto.class);
        String query = "INSERT INTO mm_vehicle " +
                "(customerDni, idVehicleExternal, numberPlate, vehicleType, brand, model, color, serialNumber) " +
                "VALUES " +
                "(?, ?, ?, ?, ?, ?, ?, ?)";
        try {
            for (String customerDNI : customersDNI) {
                if (vehicleDto.getDniOwnerVehicle().equalsIgnoreCase(customerDNI)) {
                    System.out.println("🚗 Insertando los datos en la tabla final de vehicle!!");
                    ps = con.prepareStatement(query);
                    ps.setString(1, customerDNI);
                    ps.setInt(2, Integer.parseInt(vehicleDto.getIdVehicleExternal()));
                    ps.setString(3, vehicleDto.getNumberPlate());
                    ps.setString(4, vehicleDto.getVehicleType());
                    ps.setString(5, vehicleDto.getBrand());
                    ps.setString(6, vehicleDto.getModel());
                    ps.setString(7, vehicleDto.getColor());
                    ps.setString(8, vehicleDto.getSerialNumber());
                    int rs3 = ps.executeUpdate();
                    System.out.println("Se han actualizado " + rs3 + " registros");
                }
            }
        } catch (SQLException e) {
            System.err.println("No ha ido bien el insertado en mm_vehicle: " + e.getMessage());
        } finally {
            if (ps != null) {
                ps.close();
            }
            if (con != null) {
                con.close();
            }
        }
    }

    private void updateIntVehicle(int id, String status, String codeError, String messageError) throws SQLException {
        con = DriverManager.getConnection("jdbc:mysql://localhost:3306/motomamidb", "root", "rootroot");
        String query = "UPDATE mm_intvehicles SET statusProcess = ?, msgError = ?, lastUpdate = sysdate(), codeError = ?, updatedBy = ? WHERE id = ?";
        PreparedStatement ps;
        try {
            ps = con.prepareStatement(query);
            ps.setString(1, status);
            ps.setString(2, messageError);
            ps.setString(3, codeError);
            ps.setString(4, user);
            ps.setInt(5, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error al hacer el update de mm_intvehicles: " + e.getMessage());
        }
    }

    /**
     * Metodo generico que usaremos por si nos introducen una fecha en un formato no aceptado ej:2020-11-22
     */
    public Date getDateFormatMM(String sDate) {
        Date dateReturn = null;
        try {
            dateReturn = new SimpleDateFormat("yyyy/MM/dd").parse(sDate);
        } catch (ParseException e) {
            try {
                String[] fecha = sDate.split("-");
                if (fecha.length == 3) {
                    String nuevaFecha = fecha[0] + "/" + fecha[1] + "/" + fecha[2];
                    dateReturn = new SimpleDateFormat("yyyy/MM/dd").parse(nuevaFecha);
                } else {
                    System.err.println("FORMATO INCORRECTO");
                }
            } catch (ParseException ex) {
                System.err.println("Error al convertir la fecha: " + sDate + ex.getMessage());
            } catch (Exception ex) {
                System.err.println("Error: " + ex.getMessage());
            }
        } catch (Exception e) {
            System.err.println("Error desconocido al convertir la fecha: " + sDate + e.getMessage());
        }
        return dateReturn;
    }

    /**
     * Metodo que lee el fichero de los partes.
     */
    public void readFileInfoParts() {
        System.out.println("💡Llamada a readFileInfoParts");
        List<PartDto> listOfParts = new ArrayList<>();
        String provider = pathFolderProviders.split(";")[0];
        String filePath = pathFolderinFiles + "/" + provider + "/" + pathFolderPartsProviders.split(";")[0] + extensionFileProviders;
        GsonBuilder gsonBuilder = new GsonBuilder().serializeNulls().setPrettyPrinting();
        Gson gson = gsonBuilder.setPrettyPrinting().setDateFormat("yyyy/MM/dd").create();
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String linea;
            int numlinea = 0;
            while ((linea = br.readLine()) != null) {
                if (numlinea != 0) {
                    String[] vehicles = linea.split(";");
                    PartDto part = new PartDto();
                    fillPartsDto(linea, part);
                    listOfParts.add(part);
                    String partsString = gson.toJson(part);
                    boolean existsJson = existInfoParts(partsString);
                    boolean existsId = existIdExternalParts(vehicles[0]);
                    if (existsId) {
                        if (existsJson) {
                            System.out.println("Existe el idExternal");
                            System.out.println("Ya exsiste este parte");
                        } else {
                            insertIntParts(partsString, linea, getOperationUpdate());
                            System.out.println("Se han actualizado los datos de mm_intparts");
                        }
                    } else {
                        insertIntParts(partsString, linea, getOperationNew());
                        System.out.println("📝 Añadiendo los datos nuevos en mm_intparts");
                    }
                }
                System.out.println("Elemento " + listOfParts.size());
                numlinea++;
            }
        } catch (FileNotFoundException e) {
            System.err.println("El archivo especificado no se ha encontrado, revise la ruta: " + e.getMessage());
        } catch (IOException e) {
            System.err.println("Ha ocurrido un error durante la lectura del archivo: " + e.getMessage());
        } catch (SQLException e) {
            System.err.println("Ha ocurrido un error al insertar en mm_intparts: " + e.getMessage());
        }
    }

    public boolean existIdExternalParts(String idExternal) throws SQLException {
        con = DriverManager.getConnection("jdbc:mysql://localhost:3306/motomamidb", "root", "rootroot");
        String query = "SELECT COUNT(*) AS idExternal FROM mm_intparts WHERE idExternal = ?;";
        PreparedStatement ps;
        ResultSet rs;
        int count = 0;
        try {
            ps = con.prepareStatement(query);
            ps.setString(1, idExternal);
            rs = ps.executeQuery();
            if (rs.next()) {
                count = rs.getInt("idExternal");
            }
            return count > 0;
        } catch (SQLException e) {
            System.err.println("Error executing SELECT query(IdExternalVehicle) mm_parts: " + e.getMessage());
            return false;
        }
    }

    public void insertIntParts(String p_json, String linea, String operacion) throws SQLException {
        con = DriverManager.getConnection("jdbc:mysql://localhost:3306/motomamidb", "root", "rootroot");
        String prov = pathFolderProviders.split(";")[0];
        String creator = "mm_app";
        String[] vehiculos = linea.split(";");
        LocalDateTime fechaHoraActual = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String fechaFormateada = fechaHoraActual.format(formatter);
        String query = "INSERT INTO mm_intparts " +
                "(idProv, contJson, creationDate, lastUpdate, createdBy, updatedBy, statusProcess, idExternal, operation) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = con.prepareStatement(query)) {
            ps.setString(1, prov);
            ps.setString(2, p_json);
            ps.setString(3, fechaFormateada);
            ps.setString(4, fechaFormateada);
            ps.setString(5, creator);
            ps.setString(6, creator);
            ps.setString(7, estadoFichero[0]);
            ps.setString(8, vehiculos[0]);
            ps.setString(9, operacion);
            int rs3 = ps.executeUpdate();
            System.out.println("Se han actualizado " + rs3 + " registros");
        } catch (SQLException e) {
            System.err.println("Error executing insert en mm_intparts: " + e.getMessage());
        } finally {
            if (con != null) {
                con.close();
            }
        }
    }

    /**
     * Metodo que rellena un objeto parte con los datos del fichero
     * .trim() sirve para ignorar los espacios que metan en el fichero
     */
    public void fillPartsDto(String linea, PartDto part) {
        String[] partes = linea.split(";");
        System.out.println(linea);
        part.setIdExternal(partes[0].trim());
        part.setDescriptionPartExternal(partes[2].trim());
        part.setCodeDamageExternal(partes[3].trim());
        part.setIdentityCode(partes[4].trim());
        part.setDatePartExternal(getDateFormatMM(partes[1].trim()));
    }

    /**
     * Metodo que comprueba si existe el infoParts
     */
    public boolean existInfoParts(String p_json) throws SQLException {
        con = DriverManager.getConnection("jdbc:mysql://localhost:3306/motomamidb", "root", "rootroot");
        String query = "SELECT COUNT(*) AS numParts FROM mm_intparts WHERE contJson = ?;";
        PreparedStatement ps = null;
        ResultSet rs = null;
        int numParts = 0;
        try {
            ps = con.prepareStatement(query);
            ps.setString(1, p_json);
            rs = ps.executeQuery();
            while (rs.next()) {
                numParts = rs.getInt("numParts");
            }
            return numParts > 0;
        } catch (SQLException e) {
            System.err.println("Error executing SELECT query: " + e.getMessage());
            return false;
        } finally {
            if (rs != null) {
                rs.close();
            }
            if (ps != null) {
                ps.close();
            }
            if (con != null) {
                con.close();
            }
        }
    }

    /**
     * Metodo que lee el fichero de clientes.
     */
    public void readFileInfoCustomer() {
        System.out.println("💡Llamada a readFileInfoCustomer");
        List<CustomerDto> listOfCustomer = new ArrayList<CustomerDto>();
        String provider = pathFolderProviders.split(";")[0];
        String filePath = pathFolderinFiles + "/" + provider + "/" + pathFolderCustomersProviders.split(";")[0] + extensionFileProviders;
        System.out.println(filePath);
        GsonBuilder gsonBuilder = new GsonBuilder().serializeNulls().setPrettyPrinting();
        Gson gson = gsonBuilder.setPrettyPrinting().setDateFormat("yyyy/MM/dd").create();
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String linea;
            int numlinea = 0;
            while ((linea = br.readLine()) != null) {
                if (numlinea != 0) {
                    CustomerDto customer = new CustomerDto();
                    fillCostumerDto(linea, customer);
                    listOfCustomer.add(customer);
                    System.out.println(customer.toString());
                    String customerString = gson.toJson(customer);
                    boolean existsCustomer;
                    existsCustomer = existsIdExternal(customer.getDNI(), TABLE_NAME.mm_intcustomers.name());
                    if (existsCustomer) {
                        if (isSameInfoCustomer(customerString)) {
                            System.out.println("Existe el cliente");
                        } else {
                            insertIntCustomer(customerString, linea, getOperationUpdate());
                        }
                    } else {
                        insertIntCustomer(customerString, linea, getOperationNew());
                        System.out.println("Se han añadido los datos");
                    }
                }
                numlinea++;
                System.out.println("Elemento " + listOfCustomer.size());
            }
        } catch (FileNotFoundException e) {
            System.err.println("🤦‍ El archivo especificado no se ha encontrado, revisa la ruta: " + e.getMessage());
        } catch (IOException e) {
            System.err.println("🤦‍ Ha ocurrido un error durante la lectura del archivo: " + e.getMessage());
        } catch (SQLException e) {
            System.err.println("Error con la base de datos: " + e.getMessage());
        }
    }

    /**
     * Metodo que rellena un objeto cliente con los datos del fichero.
     */
    private void fillCostumerDto(String linea, CustomerDto customer) {
        String[] clientes = linea.split(";");
        String dni = clientes[0].trim();
        String nombre = clientes[1].trim();
        String apellido1 = clientes[2].trim();
        String apellido2 = clientes[3].trim();
        String email = clientes[4].trim();
        String sDateBirth = clientes[5].trim();
        DireccionDto dre = new DireccionDto();
        String telefono = clientes[10].trim();
        String sexo = clientes[11].trim();
        System.out.println(linea);
        customer.setDNI(dni);
        customer.setNombre(nombre);
        customer.setApellido1(apellido1);
        customer.setApellido2(apellido2);
        customer.setEmail(email);
        customer.setDireccion(dre);
        customer.getDireccion().setTipoVia(clientes[7]);
        customer.getDireccion().setCodPostal(clientes[6]);
        customer.getDireccion().setCiudad(clientes[8]);
        customer.getDireccion().setNumero(clientes[9]);
        customer.setTelefono(telefono);
        customer.setSexo(sexo);
        customer.setFechaNacimiento(getDateFormatMM(sDateBirth));
    }

    /**
     * Metodo que se conecta a la base de datos y comprueba que el json sea igual.
     */
    public boolean isSameInfoCustomer(String p_json) throws SQLException {
        con = DriverManager.getConnection("jdbc:mysql://localhost:3306/motomamidb", "root", "rootroot");
        String query = "select count(*) as numCustomer from mm_intcustomers where contJson = ?;";
        PreparedStatement ps = null;
        ResultSet rs = null;
        int count = 0;
        try {
            System.out.println("LLamada al existe");
            ps = con.prepareStatement(query);
            ps.setString(1, p_json);
            rs = ps.executeQuery();
            while (rs.next()) {
                count = rs.getInt("numCustomer");
            }
            return count > 0;
        } catch (SQLException e) {
            System.err.println("Problemas con la base de datos " + e.getMessage());
            return false;
        } catch (Exception e) {
            System.err.println("ERROR: " + e.getMessage());
            return false;
        } finally {
            if (rs != null) {
                rs.close();
            }
            if (ps != null) {
                ps.close();
            }
            if (con != null) {
                con.close();
            }
        }
    }

    public int insertAddress(DireccionDto address) throws SQLException {
        con = DriverManager.getConnection("jdbc:mysql://localhost:3306/motomamidb", "root", "rootroot");
        PreparedStatement ps;
        ResultSet rs;
        String query = "INSERT INTO mm_address " +
                "(street, st_number, city, post_code) " +
                "VALUES " +
                "(?, ?, ?, ?)";
        ps = con.prepareStatement(query);
        ps.setString(1, address.getTipoVia());
        ps.setInt(2, Integer.parseInt(address.getNumero()));
        ps.setString(3, address.getCiudad());
        ps.setString(4, address.getCodPostal());
        ps.executeUpdate();

        String getQueryId = "SELECT id FROM mm_address WHERE upper(street) = upper(?) AND upper(st_number) = upper(?) AND upper(city) = upper(?) AND upper(post_code) = upper(?)";
        ps = con.prepareStatement(getQueryId);
        ps.setString(1, address.getTipoVia());
        ps.setInt(2, Integer.parseInt(address.getNumero()));
        ps.setString(3, address.getCiudad());
        ps.setString(4, address.getCodPostal());
        rs = ps.executeQuery();
        int id = -1;
        while (rs.next()) {
            id = rs.getInt("id");
        }
        return id;
    }

    public void insertCustomer(String infoCustomerJSON) throws SQLException {
        con = DriverManager.getConnection("jdbc:mysql://localhost:3306/motomamidb", "root", "rootroot");
        PreparedStatement ps = null;
        GsonBuilder gsonBuilder = new GsonBuilder().serializeNulls().setPrettyPrinting();
        Gson gson = gsonBuilder.setPrettyPrinting().setDateFormat("yyyy/MM/dd").create();
        CustomerDto customerDto = gson.fromJson(infoCustomerJSON, CustomerDto.class);
        String query = "INSERT INTO mm_customer " +
                "(dni, customer_name, surname1, surname2, email, fecha_nacimiento, gender, address_id) " +
                "VALUES " +
                "(?, ?, ?, ?, ?, ?, ?, ?)";
        try {
            int id = insertAddress(customerDto.getDireccion());
            if (id > -1) {
                System.out.println("🚀 Insertando los datos en la tabla final de mm_intcustomers!!");
                ps = con.prepareStatement(query);
                ps.setString(1, customerDto.getDNI());
                ps.setString(2, customerDto.getNombre());
                ps.setString(3, customerDto.getApellido1());
                ps.setString(4, customerDto.getApellido2());
                ps.setString(5, customerDto.getEmail());
                ps.setDate(6, new java.sql.Date(customerDto.getFechaNacimiento().getTime()));
                ps.setString(7, customerDto.getSexo());
                ps.setInt(8, id);
                int rs3 = ps.executeUpdate();
                System.out.println("Se han actualizado " + rs3 + " registros");
            }
        } catch (SQLException e) {
            System.err.println("No ha ido bien el insertado: " + e.getMessage());
        } finally {
            if (ps != null) {
                ps.close();
            }
            if (con != null) {
                con.close();
            }
        }
    }

    /**
     * Metodo que comprueba si existe el idExternal
     */
    public boolean existsIdExternal(String idExternal, String tableName) throws SQLException {
        con = DriverManager.getConnection("jdbc:mysql://localhost:3306/motomamidb", "root", "rootroot");
        String query = "select count(*) as idExternal from " + tableName + "  where idExternal = ?;";
        PreparedStatement ps = null;
        ResultSet rs = null;
        int count = 0;
        try {
            System.out.println("LLamada al idExternal");
            ps = con.prepareStatement(query);
            ps.setString(1, idExternal);
            rs = ps.executeQuery();
            while (rs.next()) {
                count = rs.getInt("idExternal");
            }
            return count > 0;
        } catch (SQLException e) {
            System.err.println("ERROR - select: " + e.getMessage());
            return false;
        } catch (Exception e) {
            System.err.println("ERROR: " + e.getMessage());
            return false;
        } finally {
            if (rs != null) {
                rs.close();
            }
            if (ps != null) {
                ps.close();
            }
            if (con != null) {
                con.close();
            }
        }
    }

    /**
     * Si no existe en la tabla intCustomer hace el insert
     */
    public void insertIntCustomer(String p_json, String linea, String operation) throws SQLException {
        con = DriverManager.getConnection("jdbc:mysql://localhost:3306/motomamidb", "root", "rootroot");
        String prov = "bbva";
        String creador = "mm_app";
        String[] clientes = linea.split(";");
        LocalDateTime fechaHoraActual = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String fechaFormateada = fechaHoraActual.format(formatter);
        PreparedStatement ps = null;
        String query = "INSERT INTO mm_intcustomers " +
                "(idProv, contJson, creationDate, lastUpdate, createdBy, updatedBy, idExternal, statusProcess, msgError, codeError, operation) " +
                "VALUES " +
                "(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try {
            System.out.println("🚀 Insertando los datos en la tabla mm_intcustomers!!");
            ps = con.prepareStatement(query);
            ps.setString(1, prov);
            ps.setBytes(2, p_json.getBytes(StandardCharsets.UTF_8));
            ps.setString(3, fechaFormateada);
            ps.setString(4, fechaFormateada);
            ps.setString(5, creador);
            ps.setString(6, creador);
            ps.setString(7, clientes[0].trim());
            ps.setString(8, getStatusNotProcessed());
            ps.setString(9, "");
            ps.setString(10, "");
            ps.setString(11, operation);
            int rs3 = ps.executeUpdate();
            System.out.println("Se han actualizado " + rs3 + " registros");
        } catch (SQLException e) {
            System.err.println("No funciona insertando: " + e.getMessage());
        } finally {
            if (ps != null) {
                ps.close();
            }
            if (con != null) {
                con.close();
            }
        }
    }

    private String getStatusNotProcessed() {
        return estadoFichero[0];
    }

    private String getOperationNew() {
        return operation[0];
    }

    private String getOperationUpdate() {
        return operation[1];
    }

    /**
     * Metodo que lee el fichero de vehiculos.
     */
    public void readFileInfoVehicles() {
        System.out.println("💡Llamada a readFileInfoVehicles");
        List<VehicleDto> listOfVehiculo = new ArrayList<>();
        GsonBuilder gsonBuilder = new GsonBuilder().serializeNulls().setPrettyPrinting();
        Gson gson = gsonBuilder.setPrettyPrinting().setDateFormat("yyyy/MM/dd").create();
        String provider = pathFolderProviders.split(";")[0];
        String filePath = pathFolderinFiles + "/" + provider + "/" + pathFolderVehiclesProviders.split(";")[0] + extensionFileProviders;
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String linea;
            int numlinea = 0;
            while ((linea = br.readLine()) != null) {
                if (numlinea != 0) {
                    String[] vehicles = linea.split(";");
                    VehicleDto vehiculoDto = new VehicleDto();
                    fillVehicleDto(linea, vehiculoDto);
                    listOfVehiculo.add(vehiculoDto);
                    String vehicleString = gson.toJson(vehiculoDto);
                    boolean existsID = existIdExternalVehicle(vehicles[0]);
                    boolean existsJson = existJsonVehicle(vehicleString);
                    if (existsID) {
                        System.out.println("Existe el idExternal");
                        if (existsJson) {
                            System.out.println("Ya existe en la base de datos");
                        } else {
                            insertIntVehicle(vehicleString, linea, getOperationUpdate());
                            System.out.println("🚗 Se han actualizado los datos en mm_intvehicle");
                        }
                    } else {
                        insertIntVehicle(vehicleString, linea, getOperationNew());
                        System.out.println("🚗 Se han añadido datos en mm_intvehicle");
                    }
                }
                System.out.println("Elemento " + listOfVehiculo.size());
                numlinea++;
            }
        } catch (FileNotFoundException e) {
            System.err.println("🚗 El archivo especificado no se ha encontrado, revise la ruta: " + e.getMessage());
        } catch (IOException e) {
            System.err.println("🚗 Ha ocurrido un error durante la lectura del archivo: " + e.getMessage());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Metodo que rellena un objeto vehiculo con los datos del vehiculo.
     */
    public void fillVehicleDto(String linea, VehicleDto vehiculo) {
        String[] vehiculos = linea.split(";");
        String owner = vehiculos[0].trim();
        String id = vehiculos[2].trim();
        String numberPlate = vehiculos[3].trim();
        String tipoVehiculo = vehiculos[4].trim();
        String marca = vehiculos[5].trim();
        String modelo = vehiculos[6].trim();
        String color = vehiculos[7].trim();
        String numeroSerial = vehiculos[8].trim();
        System.out.println(linea);
        vehiculo.setDniOwnerVehicle(owner);
        vehiculo.setIdVehicleExternal(id);
        vehiculo.setNumberPlate(numberPlate);
        vehiculo.setVehicleType(tipoVehiculo);
        vehiculo.setBrand(marca);
        vehiculo.setModel(modelo);
        vehiculo.setColor(color);
        vehiculo.setSerialNumber(numeroSerial);
    }

    public boolean existJsonVehicle(String p_json) throws SQLException {
        con = DriverManager.getConnection("jdbc:mysql://localhost:3306/motomamidb", "root", "rootroot");
        String query = "SELECT COUNT(*) AS numVehicle FROM mm_intvehicles WHERE contJson = ?;";
        PreparedStatement ps;
        ResultSet rs;
        int numVehicles = 0;
        try {
            ps = con.prepareStatement(query);
            ps.setString(1, p_json);
            rs = ps.executeQuery();
            while (rs.next()) {
                numVehicles = rs.getInt("numVehicle");
            }
            return numVehicles > 0;
        } catch (SQLException e) {
            System.err.println("Error executing SELECT query: " + e.getMessage());
            return false;
        }
    }

    public boolean existIdExternalVehicle(String idExternal) throws SQLException {
        con = DriverManager.getConnection("jdbc:mysql://localhost:3306/motomamidb", "root", "rootroot");
        String query = "SELECT COUNT(*) AS idExternal FROM mm_intvehicles WHERE idExternal = ?;";
        PreparedStatement ps;
        ResultSet rs;
        int count = 0;
        try {
            ps = con.prepareStatement(query);
            ps.setString(1, idExternal);
            rs = ps.executeQuery();
            if (rs.next()) {
                count = rs.getInt("idExternal");
            }
            return count > 0;
        } catch (SQLException e) {
            System.err.println("Error executing SELECT query(IdExternalVehicle): " + e.getMessage());
            return false;
        }
    }


    public void insertIntVehicle(String p_json, String linea, String operacion) throws SQLException {
        con = DriverManager.getConnection("jdbc:mysql://localhost:3306/motomamidb", "root", "rootroot");
        String prov = pathFolderProviders.split(";")[0];
        String[] vehiculos = linea.split(";");
        String creator = "mm_app";
        LocalDateTime fechaHoraActual = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String fechaFormateada = fechaHoraActual.format(formatter);
        String query = "INSERT INTO mm_intvehicles " +
                "(idProv, contJson, creationDate, lastUpdate, createdBy, updatedBy, statusProcess, idExternal, msgError, codeError, operation) " +
                "VALUES " +
                "(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = con.prepareStatement(query)) {
            ps.setString(1, prov);
            ps.setString(2, p_json);
            ps.setString(3, fechaFormateada);
            ps.setString(4, fechaFormateada);
            ps.setString(5, creator);
            ps.setString(6, creator);
            ps.setString(7, getStatusNotProcessed());
            ps.setString(8, vehiculos[0].trim());
            ps.setString(9, "");
            ps.setString(10, "");
            ps.setString(11, operacion);
            int rs3 = ps.executeUpdate();
            System.out.println("Se han actualizado " + rs3 + " registros");
        } catch (SQLException e) {
            System.err.println("Error executing INSERT query: " + e.getMessage());
        } finally {
            if (con != null) {
                con.close();
            }
        }
    }
}
