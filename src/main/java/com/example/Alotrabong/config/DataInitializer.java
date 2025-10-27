package com.example.Alotrabong.config;

import com.example.Alotrabong.entity.*;
import com.example.Alotrabong.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final CategoryRepository categoryRepository;
    private final BranchRepository branchRepository;
    private final ItemRepository itemRepository;

    // Lưu categories để tái sử dụng
    private Category catCom;
    private Category catMi;
    private Category catTrangMieng;
    private Category catNuoc;

    @Override
    public void run(String... args) throws Exception {
        initializeRoles();
        initializeCategories();
        initializeBranches();
        initializeItems();
    }

    private void initializeRoles() {
        // Tạo các role cơ bản nếu chưa có
        createRoleIfNotExists(RoleCode.ADMIN, "Administrator");
        createRoleIfNotExists(RoleCode.BRANCH_MANAGER, "Branch Manager");
        createRoleIfNotExists(RoleCode.SHIPPER, "Shipper");
        createRoleIfNotExists(RoleCode.USER, "User");
        
        log.info("Role initialization completed");
    }

    private void createRoleIfNotExists(RoleCode roleCode, String roleName) {
        if (!roleRepository.findByRoleCode(roleCode).isPresent()) {
            Role role = Role.builder()
                    .roleCode(roleCode)
                    .roleName(roleName)
                    .build();
            roleRepository.save(role);
            log.info("Created role: {} - {}", roleCode, roleName);
        } else {
            log.debug("Role {} already exists", roleCode);
        }
    }

    private void initializeCategories() {
        if (categoryRepository.count() > 0) {
            log.info("Categories already initialized - loading existing categories");
            // Load existing categories
            loadExistingCategories();
            return;
        }

        catCom = Category.builder()
                .name("Cơm")
                .description("Các món cơm ngon")
                .isActive(true)
                .build();
        catCom = categoryRepository.save(catCom);
        log.info("Created category: {} (ID: {})", catCom.getName(), catCom.getCategoryId());

        catMi = Category.builder()
                .name("Mì")
                .description("Các món mì, phở, bún")
                .isActive(true)
                .build();
        catMi = categoryRepository.save(catMi);
        log.info("Created category: {} (ID: {})", catMi.getName(), catMi.getCategoryId());

        catTrangMieng = Category.builder()
                .name("Tráng miệng")
                .description("Đồ ngọt và tráng miệng")
                .isActive(true)
                .build();
        catTrangMieng = categoryRepository.save(catTrangMieng);
        log.info("Created category: {} (ID: {})", catTrangMieng.getName(), catTrangMieng.getCategoryId());

        catNuoc = Category.builder()
                .name("Đồ uống")
                .description("Nước giải khát")
                .isActive(true)
                .build();
        catNuoc = categoryRepository.save(catNuoc);
        log.info("Created category: {} (ID: {})", catNuoc.getName(), catNuoc.getCategoryId());

        log.info("Categories initialization completed");
    }

    private void loadExistingCategories() {
        // Load categories theo thứ tự để tránh lỗi encoding
        java.util.List<Category> allCategories = categoryRepository.findAll();
        log.info("Found {} existing categories", allCategories.size());
        
        for (Category cat : allCategories) {
            log.info("Existing category: {} (ID: {})", cat.getName(), cat.getCategoryId());
        }
        
        // Gán theo index nếu tìm được
        if (allCategories.size() >= 4) {
            catCom = allCategories.get(0);
            catMi = allCategories.get(1);
            catTrangMieng = allCategories.get(2);
            catNuoc = allCategories.get(3);
        }
    }

    private void initializeBranches() {
        if (branchRepository.count() > 0) {
            log.info("Branches already initialized");
            return;
        }

        Branch hcm = Branch.builder()
                .branchCode("HCM01")
                .name("AloTraBong - TP.HCM")
                .address("123 Nguyễn Huệ, Quận 1")
                .district("Quận 1")
                .city("TP. Hồ Chí Minh")
                .phone("0901234567")
                .latitude(new BigDecimal("10.775270"))
                .longitude(new BigDecimal("106.702130"))
                .openHours("7:00 - 22:00")
                .isActive(true)
                .build();
        branchRepository.save(hcm);

        Branch hn = Branch.builder()
                .branchCode("HN01")
                .name("AloTraBong - Hà Nội")
                .address("456 Hoàn Kiếm, Quận Hoàn Kiếm")
                .district("Quận Hoàn Kiếm")
                .city("Hà Nội")
                .phone("0902345678")
                .latitude(new BigDecimal("21.028511"))
                .longitude(new BigDecimal("105.804817"))
                .openHours("7:00 - 22:00")
                .isActive(true)
                .build();
        branchRepository.save(hn);

        Branch dn = Branch.builder()
                .branchCode("DN01")
                .name("AloTraBong - Đà Nẵng")
                .address("789 Trần Phú, Quận Hải Châu")
                .district("Quận Hải Châu")
                .city("Đà Nẵng")
                .phone("0903456789")
                .latitude(new BigDecimal("16.068089"))
                .longitude(new BigDecimal("108.212769"))
                .openHours("7:00 - 22:00")
                .isActive(true)
                .build();
        branchRepository.save(dn);

        log.info("Branches initialization completed");
    }

    private void initializeItems() {
        if (itemRepository.count() > 0) {
            log.info("Items already initialized");
            return;
        }

        // Kiểm tra categories đã được load
        if (catCom == null || catMi == null || catTrangMieng == null || catNuoc == null) {
            log.error("Cannot initialize items: Categories references are null (Cơm: {}, Mì: {}, Tráng miệng: {}, Đồ uống: {})",
                    catCom != null, catMi != null, catTrangMieng != null, catNuoc != null);
            return;
        }

        log.info("All categories are ready - Starting items initialization...");

        // Món cơm
        Item comTamSuon = Item.builder()
                .itemCode("COM001")
                .category(catCom)
                .name("Cơm Tấm Sườn Nướng")
                .description("Cơm tấm sườn nướng thơm ngon, kèm trứng và chả")
                .price(new BigDecimal("45000"))
                .calories(650)
                .isActive(true)
                .build();
        itemRepository.save(comTamSuon);

        Item comGa = Item.builder()
                .itemCode("COM002")
                .category(catCom)
                .name("Cơm Gà Xối Mỡ")
                .description("Cơm gà xối mỡ Hội An đặc sản")
                .price(new BigDecimal("50000"))
                .calories(580)
                .isActive(true)
                .build();
        itemRepository.save(comGa);

        Item comChien = Item.builder()
                .itemCode("COM003")
                .category(catCom)
                .name("Cơm Chiên Dương Châu")
                .description("Cơm chiên đủ vị, nhiều topping")
                .price(new BigDecimal("40000"))
                .calories(720)
                .isActive(true)
                .build();
        itemRepository.save(comChien);

        // Món mì
        Item pho = Item.builder()
                .itemCode("MI001")
                .category(catMi)
                .name("Phở Bò Tái")
                .description("Phở bò Hà Nội chính gốc")
                .price(new BigDecimal("55000"))
                .calories(450)
                .isActive(true)
                .build();
        itemRepository.save(pho);

        Item bun = Item.builder()
                .itemCode("MI002")
                .category(catMi)
                .name("Bún Bò Huế")
                .description("Bún bò Huế cay nồng đậm đà")
                .price(new BigDecimal("50000"))
                .calories(500)
                .isActive(true)
                .build();
        itemRepository.save(bun);

        Item miXao = Item.builder()
                .itemCode("MI003")
                .category(catMi)
                .name("Mì Xào Hải Sản")
                .description("Mì xào giòn với hải sản tươi")
                .price(new BigDecimal("60000"))
                .calories(620)
                .isActive(true)
                .build();
        itemRepository.save(miXao);

        // Tráng miệng
        Item che = Item.builder()
                .itemCode("TM001")
                .category(catTrangMieng)
                .name("Chè Khúc Bạch")
                .description("Chè khúc bạch mát lạnh")
                .price(new BigDecimal("25000"))
                .calories(280)
                .isActive(true)
                .build();
        itemRepository.save(che);

        Item banh = Item.builder()
                .itemCode("TM002")
                .category(catTrangMieng)
                .name("Bánh Flan")
                .description("Bánh flan caramen mềm mịn")
                .price(new BigDecimal("20000"))
                .calories(220)
                .isActive(true)
                .build();
        itemRepository.save(banh);

        // Đồ uống
        Item tra = Item.builder()
                .itemCode("NU001")
                .category(catNuoc)
                .name("Trà Đào Cam Sả")
                .description("Trà trái cây tươi mát")
                .price(new BigDecimal("35000"))
                .calories(150)
                .isActive(true)
                .build();
        itemRepository.save(tra);

        Item cafe = Item.builder()
                .itemCode("NU002")
                .category(catNuoc)
                .name("Cà Phê Sữa Đá")
                .description("Cà phê phin truyền thống")
                .price(new BigDecimal("30000"))
                .calories(180)
                .isActive(true)
                .build();
        itemRepository.save(cafe);

        Item sinh = Item.builder()
                .itemCode("NU003")
                .category(catNuoc)
                .name("Sinh Tố Bơ")
                .description("Sinh tố bơ sánh mịn")
                .price(new BigDecimal("40000"))
                .calories(320)
                .isActive(true)
                .build();
        itemRepository.save(sinh);

        log.info("Items initialization completed - {} items created", itemRepository.count());
    }
}