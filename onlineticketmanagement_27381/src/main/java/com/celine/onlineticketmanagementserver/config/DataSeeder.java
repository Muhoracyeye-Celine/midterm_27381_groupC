package com.celine.onlineticketmanagementserver.config;

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.celine.onlineticketmanagementserver.enums.LocationType;
import com.celine.onlineticketmanagementserver.enums.RoleType;
import com.celine.onlineticketmanagementserver.model.Location;
import com.celine.onlineticketmanagementserver.model.Person;
import com.celine.onlineticketmanagementserver.model.Role;
import com.celine.onlineticketmanagementserver.repository.LocationRepository;
import com.celine.onlineticketmanagementserver.repository.PersonRepository;
import com.celine.onlineticketmanagementserver.repository.RoleRepository;

/**
 * Seeds initial application data on startup.
 * Populates roles, Rwanda administrative locations,
 * and a default admin user.
 */
@Component
public class DataSeeder implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DataSeeder.class);

    private final LocationRepository locationRepository;
    private final RoleRepository roleRepository;
    private final PersonRepository personRepository;
    private final PasswordEncoder passwordEncoder;

    // Maps to store created locations for reference
    private final Map<String, Location> provinces = new HashMap<>();
    private final Map<String, Location> districts = new HashMap<>();
    private final Map<String, Location> sectors = new HashMap<>();
    private final Map<String, Location> cells = new HashMap<>();

    public DataSeeder(LocationRepository locationRepository,
                      RoleRepository roleRepository,
                      PersonRepository personRepository,
                      PasswordEncoder passwordEncoder) {
        this.locationRepository = locationRepository;
        this.roleRepository = roleRepository;
        this.personRepository = personRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        try {
            seedRoles();

            if (locationRepository.count() > 0) {
                logger.info("Locations already exist in the database. Skipping location seeding.");
            } else {
                logger.info("Starting Rwanda location data seeding...");

                seedProvinces();
                seedDistricts();
                seedSectors();
                seedCells();
                seedVillages();

                long totalCount = locationRepository.count();
                logger.info("Location data seeding completed successfully. Total locations: {}", totalCount);
            }

            seedAdminUser();

        } catch (Exception e) {
            logger.error("Error occurred during data seeding", e);
        }
    }

    private void seedRoles() {
        logger.info("Seeding roles...");

        if (roleRepository.count() > 0) {
            logger.info("Roles already exist in the database. Skipping role seeding.");
            return;
        }

        String[][] roleData = {
            {"ADMIN", "System administrator with full access"},
            {"USER", "Regular user who can book tickets and attend events"},
            {"VENUE_MANAGER", "Manager responsible for venue operations and bookings"},
            {"ORGANIZER", "Event organizer who can create and manage events"},
            {"TICKET_SCANNER", "Staff member who can scan and validate tickets at events"},
            {"CUSTOMER_SUPPORT", "Support staff who can assist users and handle inquiries"}
        };

        for (String[] data : roleData) {
            Role role = new Role();
            role.setName(RoleType.valueOf(data[0]));
            role.setDescription(data[1]);
            roleRepository.save(role);
            logger.debug("Seeded role: {}", data[0]);
        }

        logger.info("Seeded {} roles successfully.", roleData.length);
    }

    private void seedAdminUser() {
        logger.info("Seeding default admin user...");

        if (personRepository.existsByUsername("admin")) {
            logger.info("Admin user 'admin' already exists. Updating credentials...");
            personRepository.findByUsername("admin").ifPresent(existingAdmin -> {
                existingAdmin.setUsername("admin");
                existingAdmin.setFirstName("Celine");
                existingAdmin.setLastName("Muhoracyeye");
                existingAdmin.setEmail("muhoracyeyecln@gmail.com");
                existingAdmin.setPhone("+250788139992");
                existingAdmin.setPassword(passwordEncoder.encode("celine123"));
                existingAdmin.setEmailVerified(true);
                existingAdmin.setEnabled(true);
                existingAdmin.setAccountNonExpired(true);
                existingAdmin.setAccountNonLocked(true);
                existingAdmin.setCredentialsNonExpired(true);
                existingAdmin.setFailedLoginAttempts(0);
                existingAdmin.setLockTime(null);
                Location defaultVillage = locationRepository.findByType(LocationType.VILLAGE).stream()
                        .findFirst()
                        .orElse(null);
                if (defaultVillage != null && (existingAdmin.getLivesIn() == null || existingAdmin.getLivesIn().getType() != LocationType.VILLAGE)) {
                    existingAdmin.setLivesIn(defaultVillage);
                }
                existingAdmin.setUpdatedAt(new Date());

                personRepository.save(existingAdmin);
                logger.info("Admin user credentials updated successfully.");
            });
            return;
        }

        if (personRepository.existsByEmail("muhoracyeyecln@gmail.com")) {
            logger.info("Admin user with email 'muhoracyeyecln@gmail.com' already exists. Skipping admin user seeding.");
            return;
        }

        try {
            Role adminRole = roleRepository.findByName(RoleType.ADMIN)
                    .orElseThrow(() -> new RuntimeException("ADMIN role not found"));

            Location defaultVillage = locationRepository.findByType(LocationType.VILLAGE).stream()
                    .findFirst()
                    .orElse(null);

            if (defaultVillage == null) {
                logger.warn("No villages found. Cannot create admin user without a village.");
                return;
            }

            Person admin = new Person();
            admin.setUsername("admin");
            admin.setFirstName("Celine");
            admin.setLastName("Muhoracyeye");
            admin.setEmail("muhoracyeyecln@gmail.com");
            admin.setPhone("+250788139992");
            admin.setPassword(passwordEncoder.encode("celine123"));
            admin.setLivesIn(defaultVillage);
            admin.setEnabled(true);
            admin.setAccountNonExpired(true);
            admin.setAccountNonLocked(true);
            admin.setCredentialsNonExpired(true);
            admin.setEmailVerified(true);
            admin.setRegisteredAt(new Date());
            admin.setUpdatedAt(new Date());

            Set<Role> roles = new HashSet<>();
            roles.add(adminRole);
            admin.setRoles(roles);

            personRepository.save(admin);

            logger.info("Default admin user created successfully.");
            logger.info("Username: admin");
            logger.info("Email: muhoracyeyecln@gmail.com");
            logger.info("Please change the password after first login.");

        } catch (Exception e) {
            logger.error("Failed to create admin user", e);
        }
    }

    private void seedProvinces() {
        logger.info("Seeding provinces...");

        String[][] provinceData = {
            {"KIGALI", "KGL"},
            {"NORTHERN", "N"},
            {"SOUTHERN", "S"},
            {"EASTERN", "E"},
            {"WESTERN", "W"}
        };

        for (String[] data : provinceData) {
            Location province = new Location();
            province.setName(data[0]);
            province.setCode(data[1]);
            province.setType(LocationType.PROVINCE);
            province = locationRepository.save(province);
            provinces.put(data[0], province);
        }

        logger.info("Seeded {} provinces.", provinces.size());
    }

    private void seedDistricts() {
        logger.info("Seeding districts...");

        // Kigali Province Districts
        seedDistrict("GASABO", "GS", "KIGALI");
        seedDistrict("KICUKIRO", "KC", "KIGALI");
        seedDistrict("NYARUGENGE", "NY", "KIGALI");

        // Northern Province Districts
        seedDistrict("BURERA", "BUR", "NORTHERN");
        seedDistrict("GAKENKE", "GAK", "NORTHERN");
        seedDistrict("GICUMBI", "GIC", "NORTHERN");
        seedDistrict("MUSANZE", "MUS", "NORTHERN");
        seedDistrict("RULINDO", "RUL", "NORTHERN");

        // Southern Province Districts
        seedDistrict("GISAGARA", "GIS", "SOUTHERN");
        seedDistrict("HUYE", "HUY", "SOUTHERN");
        seedDistrict("KAMONYI", "KAM", "SOUTHERN");
        seedDistrict("MUHANGA", "MUH", "SOUTHERN");
        seedDistrict("NYAMAGABE", "NYM", "SOUTHERN");
        seedDistrict("NYANZA", "NZA", "SOUTHERN");
        seedDistrict("NYARUGURU", "NRU", "SOUTHERN");
        seedDistrict("RUHANGO", "RUH", "SOUTHERN");

        // Eastern Province Districts
        seedDistrict("BUGESERA", "BUG", "EASTERN");
        seedDistrict("GATSIBO", "GAT", "EASTERN");
        seedDistrict("KAYONZA", "KAY", "EASTERN");
        seedDistrict("KIREHE", "KIR", "EASTERN");
        seedDistrict("NGOMA", "NGO", "EASTERN");
        seedDistrict("NYAGATARE", "NYG", "EASTERN");
        seedDistrict("RWAMAGANA", "RWA", "EASTERN");

        // Western Province Districts
        seedDistrict("KARONGI", "KRG", "WESTERN");
        seedDistrict("NGORORERO", "NGR", "WESTERN");
        seedDistrict("NYABIHU", "NYB", "WESTERN");
        seedDistrict("NYAMASHEKE", "NYS", "WESTERN");
        seedDistrict("RUBAVU", "RUB", "WESTERN");
        seedDistrict("RUSIZI", "RUS", "WESTERN");
        seedDistrict("RUTSIRO", "RUT", "WESTERN");

        logger.info("Seeded {} districts.", districts.size());
    }

    private void seedSectors() {
        logger.info("Seeding sectors...");

        // Kigali - Gasabo District
        seedSector("BUMBOGO", "BUM-GS", "GASABO");
        seedSector("GATSATA", "GAT-GS", "GASABO");
        seedSector("GIKOMERO", "GIK-GS", "GASABO");
        seedSector("GISOZI", "GIS-GS", "GASABO");
        seedSector("JABANA", "JAB-GS", "GASABO");
        seedSector("JALI", "JAL-GS", "GASABO");
        seedSector("KACYIRU", "KAC-GS", "GASABO");
        seedSector("KIMIHURURA", "KIM-GS", "GASABO");
        seedSector("KIMIRONKO", "KMR-GS", "GASABO");
        seedSector("KINYINYA", "KIN-GS", "GASABO");
        seedSector("NDERA", "NDE-GS", "GASABO");
        seedSector("NDUBA", "NDU-GS", "GASABO");
        seedSector("REMERA", "REM-GS", "GASABO");
        seedSector("RUSORORO", "RUS-GS", "GASABO");
        seedSector("RUTUNGA", "RUT-GS", "GASABO");

        // Kigali - Kicukiro District
        seedSector("GAHANGA", "GAH-KC", "KICUKIRO");
        seedSector("GATENGA", "GTE-KC", "KICUKIRO");
        seedSector("GIKONDO", "GIK-KC", "KICUKIRO");
        seedSector("KAGARAMA", "KAG-KC", "KICUKIRO");
        seedSector("KANOMBE", "KAN-KC", "KICUKIRO");
        seedSector("KICUKIRO", "KIC-KC", "KICUKIRO");
        seedSector("KIGARAMA", "KIG-KC", "KICUKIRO");
        seedSector("MASAKA", "MAS-KC", "KICUKIRO");
        seedSector("NIBOYE", "NIB-KC", "KICUKIRO");
        seedSector("NYARUGUNGA", "NYR-KC", "KICUKIRO");

        // Kigali - Nyarugenge District
        seedSector("GITEGA", "GIT-NY", "NYARUGENGE");
        seedSector("KANYINYA", "KAN-NY", "NYARUGENGE");
        seedSector("KIGALI", "KGL-NY", "NYARUGENGE");
        seedSector("KIMISAGARA", "KMS-NY", "NYARUGENGE");
        seedSector("MAGERAGERE", "MAG-NY", "NYARUGENGE");
        seedSector("MUHIMA", "MUH-NY", "NYARUGENGE");
        seedSector("NYAKABANDA", "NYK-NY", "NYARUGENGE");
        seedSector("NYAMIRAMBO", "NYM-NY", "NYARUGENGE");
        seedSector("NYARUGENGE", "NYR-NY", "NYARUGENGE");
        seedSector("RWEZAMENYO", "RWE-NY", "NYARUGENGE");

        // Musanze District
        seedSector("BUSOGO", "BUS-MUS", "MUSANZE");
        seedSector("CYUVE", "CYU-MUS", "MUSANZE");
        seedSector("GACACA", "GAC-MUS", "MUSANZE");
        seedSector("GASHAKI", "GAS-MUS", "MUSANZE");
        seedSector("GATARAGA", "GAT-MUS", "MUSANZE");
        seedSector("KIMONYI", "KIM-MUS", "MUSANZE");
        seedSector("KINIGI", "KIN-MUS", "MUSANZE");
        seedSector("MUHOZA", "MUH-MUS", "MUSANZE");
        seedSector("MUKO", "MUK-MUS", "MUSANZE");
        seedSector("MUSANZE", "MUS-MUS", "MUSANZE");
        seedSector("NKOTSI", "NKO-MUS", "MUSANZE");
        seedSector("NYANGE", "NYA-MUS", "MUSANZE");
        seedSector("REMERA", "REM-MUS", "MUSANZE");
        seedSector("RWAZA", "RWA-MUS", "MUSANZE");
        seedSector("SHINGIRO", "SHI-MUS", "MUSANZE");

        // Huye District
        seedSector("GISHAMVU", "GIS-HUY", "HUYE");
        seedSector("HUYE", "HUY-HUY", "HUYE");
        seedSector("KARAMA", "KAR-HUY", "HUYE");
        seedSector("KIGOMA", "KIG-HUY", "HUYE");
        seedSector("KINAZI", "KIN-HUY", "HUYE");
        seedSector("MARABA", "MAR-HUY", "HUYE");
        seedSector("MBAZI", "MBA-HUY", "HUYE");
        seedSector("MUKURA", "MUK-HUY", "HUYE");
        seedSector("NGOMA", "NGO-HUY", "HUYE");
        seedSector("RUHASHYA", "RUH-HUY", "HUYE");
        seedSector("RUSATIRA", "RUS-HUY", "HUYE");
        seedSector("RWANIRO", "RWA-HUY", "HUYE");
        seedSector("SIMBI", "SIM-HUY", "HUYE");
        seedSector("TUMBA", "TUM-HUY", "HUYE");

        logger.info("Seeded {} sectors.", sectors.size());
    }

    private void seedCells() {
        logger.info("Seeding cells...");

        // Kacyiru Sector
        seedCell("KAMATAMU", "KAM-KAC", "KACYIRU");
        seedCell("KAMUTWA", "KMW-KAC", "KACYIRU");
        seedCell("KIBAZA", "KIB-KAC", "KACYIRU");
        seedCell("NYABISINDU", "NYB-KAC", "KACYIRU");

        // Kimironko Sector
        seedCell("BIRYOGO", "BIR-KMR", "KIMIRONKO");
        seedCell("KIBAGABAGA", "KBG-KMR", "KIMIRONKO");
        seedCell("KIMIRONKO", "KIM-KMR", "KIMIRONKO");

        // Remera Sector
        seedCell("NYAGATOVU", "NYG-REM", "REMERA");
        seedCell("NYABIKENKE", "NYB-REM", "REMERA");
        seedCell("RUGUNGA", "RUG-REM", "REMERA");
        seedCell("RUKIRI I", "RK1-REM", "REMERA");
        seedCell("RUKIRI II", "RK2-REM", "REMERA");

        // Gikondo Sector
        seedCell("GIKONDO", "GIK-GIK", "GIKONDO");
        seedCell("KABUYE", "KAB-GIK", "GIKONDO");
        seedCell("REBERO", "REB-GIK", "GIKONDO");
        seedCell("NYANZA", "NYZ-GIK", "GIKONDO");

        // Kanombe Sector
        seedCell("BUSANZA", "BUS-KAN", "KANOMBE");
        seedCell("BWERAMANA", "BWE-KAN", "KANOMBE");
        seedCell("KANOMBE", "KAN-KAN", "KANOMBE");

        // Kigali Sector
        seedCell("BIRYOGO", "BIR-KGL", "KIGALI");
        seedCell("GISIMENTI", "GIS-KGL", "KIGALI");
        seedCell("RWAMPARA", "RWA-KGL", "KIGALI");
        seedCell("NYARUGENGE", "NYR-KGL", "KIGALI");

        // Nyamirambo Sector
        seedCell("NYAKABANDA", "NYK-NYM", "NYAMIRAMBO");
        seedCell("NYAMIRAMBO", "NYM-NYM", "NYAMIRAMBO");
        seedCell("RUGENGE", "RUG-NYM", "NYAMIRAMBO");

        // Muhoza Sector
        seedCell("CYANYA", "CYA-MUH", "MUHOZA");
        seedCell("KANYINYA", "KAN-MUH", "MUHOZA");
        seedCell("MUHOZA", "MUH-MUH", "MUHOZA");
        seedCell("MUYIRA", "MUY-MUH", "MUHOZA");

        logger.info("Seeded {} cells.", cells.size());
    }

    private void seedVillages() {
        logger.info("Seeding villages...");

        int villageCount = 0;

        // Kamatamu Cell
        villageCount += seedVillage("KAMATAMU I", "KM1-KAM", "KAMATAMU");
        villageCount += seedVillage("KAMATAMU II", "KM2-KAM", "KAMATAMU");
        villageCount += seedVillage("RUGENGE", "RUG-KAM", "KAMATAMU");

        // Kibagabaga Cell
        villageCount += seedVillage("AGAKINJIRO", "AGK-KBG", "KIBAGABAGA");
        villageCount += seedVillage("KIBENGA", "KIB-KBG", "KIBAGABAGA");
        villageCount += seedVillage("NYAGATOVU", "NYG-KBG", "KIBAGABAGA");
        villageCount += seedVillage("UMUDENDE", "UMU-KBG", "KIBAGABAGA");

        // Rugunga Cell
        villageCount += seedVillage("AMAHORO", "AMA-RUG", "RUGUNGA");
        villageCount += seedVillage("KAGUGU", "KAG-RUG", "RUGUNGA");
        villageCount += seedVillage("NYARUTARAMA", "NYR-RUG", "RUGUNGA");
        villageCount += seedVillage("UBUMWE", "UBU-RUG", "RUGUNGA");

        // Gikondo Cell
        villageCount += seedVillage("GIKONDO I", "GK1-GIK", "GIKONDO");
        villageCount += seedVillage("GIKONDO II", "GK2-GIK", "GIKONDO");
        villageCount += seedVillage("GIKONDO III", "GK3-GIK", "GIKONDO");

        // Kanombe Cell
        villageCount += seedVillage("AERODROME", "AER-KAN", "KANOMBE");
        villageCount += seedVillage("AKABASOBERWA", "AKB-KAN", "KANOMBE");
        villageCount += seedVillage("KANOMBE", "KNB-KAN", "KANOMBE");
        villageCount += seedVillage("NYAGAHINGA", "NYG-KAN", "KANOMBE");

        // Biryogo Cell
        villageCount += seedVillage("BIRYOGO I", "BR1-BIR", "BIRYOGO");
        villageCount += seedVillage("BIRYOGO II", "BR2-BIR", "BIRYOGO");
        villageCount += seedVillage("BIRYOGO III", "BR3-BIR", "BIRYOGO");

        // Nyamirambo Cell
        villageCount += seedVillage("AMAHORO", "AMH-NYM", "NYAMIRAMBO");
        villageCount += seedVillage("BUTAMWA", "BUT-NYM", "NYAMIRAMBO");
        villageCount += seedVillage("MUHIMA", "MUH-NYM", "NYAMIRAMBO");
        villageCount += seedVillage("NYAMIRAMBO", "NYMV-NYM", "NYAMIRAMBO");

        // Muhoza Cell
        villageCount += seedVillage("BUSOGO", "BUS-MUH", "MUHOZA");
        villageCount += seedVillage("CYABARARIKA", "CYB-MUH", "MUHOZA");
        villageCount += seedVillage("MUHOZA", "MUHV-MUH", "MUHOZA");
        villageCount += seedVillage("URUGWIRO", "URU-MUH", "MUHOZA");

        logger.info("Seeded {} villages.", villageCount);
    }

    private void seedDistrict(String name, String code, String provinceName) {
        Location province = provinces.get(provinceName);
        if (province != null) {
            Location district = new Location();
            district.setName(name);
            district.setCode(code);
            district.setType(LocationType.DISTRICT);
            district.setParentLocation(province);
            district = locationRepository.save(district);
            districts.put(name, district);
        }
    }

    private void seedSector(String name, String code, String districtName) {
        Location district = districts.get(districtName);
        if (district != null) {
            Location sector = new Location();
            sector.setName(name);
            sector.setCode(code);
            sector.setType(LocationType.SECTOR);
            sector.setParentLocation(district);
            sector = locationRepository.save(sector);
            sectors.put(name, sector);
        }
    }

    private void seedCell(String name, String code, String sectorName) {
        Location sector = sectors.get(sectorName);
        if (sector != null) {
            Location cell = new Location();
            cell.setName(name);
            cell.setCode(code);
            cell.setType(LocationType.CELL);
            cell.setParentLocation(sector);
            cell = locationRepository.save(cell);
            cells.put(name, cell);
        }
    }

    private int seedVillage(String name, String code, String cellName) {
        Location cell = cells.get(cellName);
        if (cell != null) {
            Location village = new Location();
            village.setName(name);
            village.setCode(code);
            village.setType(LocationType.VILLAGE);
            village.setParentLocation(cell);
            locationRepository.save(village);
            return 1;
        }
        return 0;
    }
}