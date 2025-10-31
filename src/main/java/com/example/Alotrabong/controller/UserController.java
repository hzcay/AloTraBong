package com.example.Alotrabong.controller;

import com.example.Alotrabong.dto.CartItemDTO;
import com.example.Alotrabong.dto.HomeItemVM;
import com.example.Alotrabong.dto.ItemDTO;
import com.example.Alotrabong.dto.OrderDetailVM;
import com.example.Alotrabong.dto.OrderHistoryVM;
import com.example.Alotrabong.dto.BranchListDTO;
import com.example.Alotrabong.dto.AddressDTO;
import com.example.Alotrabong.dto.AddressFormDTO;
import com.example.Alotrabong.entity.*;
import com.example.Alotrabong.exception.ResourceNotFoundException;
import com.example.Alotrabong.repository.*;
import com.example.Alotrabong.service.CartService;
import com.example.Alotrabong.service.ItemService;
import com.example.Alotrabong.service.OrderHistoryService;
import com.example.Alotrabong.service.OrderService;
import com.example.Alotrabong.service.AddressService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

import org.apache.http.HttpStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    // ===== Services =====
    private final ItemService itemService;
    private final CartService cartService;
    private final OrderHistoryService orderHistoryService;
    private final AddressService addressService;
    private final OrderService orderService;

    // ===== Repositories =====
    private final ItemRepository itemRepository;
    private final ItemMediaRepository itemMediaRepository;
    private final BranchItemPriceRepository branchItemPriceRepository;
    private final InventoryRepository inventoryRepository;
    private final BranchRepository branchRepository;
    private final CategoryRepository categoryRepository;
    private final ReviewRepository reviewRepository;
    private final ReviewMediaRepository reviewMediaRepository;
    private final CouponRepository couponRepository;
    private final UserRepository userRepository;
    private final OrderRepository orderRepository;
    private final FavoriteRepository favoriteRepo;
    private final OrderItemRepository orderItemRepository;

    // ===========================
    // ====== NAV / HEADER =======
    // ===========================

    // Helper: luôn lấy tên hiển thị (fullName nếu có)
    private String resolveDisplayName(Authentication auth) {
        // chưa login -> null
        if (auth == null || !auth.isAuthenticated()) {
            return null;
        }

        String login = auth.getName(); // email/sđt/userId
        if (login == null || login.isBlank()) {
            return null;
        }

        User u = userRepository.findByLogin(login)
                .orElseGet(() -> userRepository.findById(login).orElse(null));

        if (u != null) {
            String fullName = u.getFullName();
            if (fullName != null && !fullName.isBlank()) {
                return fullName;
            }
        }
        return "/auth";
    }

    // ===== HOME =====
    @GetMapping({ "", "/" })
    public String userRoot() {
        return "redirect:/user/home";
    }

    @GetMapping({ "/home", "/home/index" })
    public String home(Model model, Authentication auth) {

        // tên user cho header
        String displayName = resolveDisplayName(auth);
        model.addAttribute("userName", displayName);

        List<HomeItemVM> productsNew = mapItems(itemService.getNewItems(8));
        List<HomeItemVM> productsBest = mapItems(itemService.getTopSellingItems(8));
        List<HomeItemVM> productsFav = mapItems(itemService.getTopFavoritedItems(8));

        model.addAttribute("productsNew", productsNew);
        model.addAttribute("productsBest", productsBest);
        model.addAttribute("productsFav", productsFav);
        model.addAttribute("recentItems", null);

        Branch defBranch = fallbackActiveBranch();
        model.addAttribute("branchId", defBranch != null ? defBranch.getBranchId() : null);

        return "user/home/index";
    }

    private List<HomeItemVM> mapItems(List<ItemDTO> src) {
        return src.stream().map(i -> HomeItemVM.builder()
                .id(i.getItemId())
                .name(i.getName())
                .price(i.getPrice())
                .thumbnailUrl("/img/products/" + i.getItemId() + ".jpg")
                .build()).collect(Collectors.toList());
    }

    // dinh dưỡng mặc định
    private static final Map<String, Object> DEFAULT_NUTRITION = Map.of(
            "cal", 315,
            "protein", 500,
            "fat", 10,
            "carb", 45,
            "sodium", 160);

    @GetMapping("/home/more")
    @ResponseBody
    public Map<String, Object> homeMore(@RequestParam String type,
            @RequestParam(required = false, defaultValue = "1000") Integer size) {

        int limit = Math.max(1, Math.min(size, 2000));

        List<ItemDTO> src;
        switch ((type == null ? "" : type).toLowerCase()) {
            case "new" -> src = itemService.getNewItems(limit);
            case "best" -> src = itemService.getTopSellingItems(limit);
            case "fav" -> src = itemService.getTopSellingItems(limit); // tạm dùng best
            default -> src = itemService.getNewItems(limit);
        }

        List<Map<String, Object>> items = src.stream().map(i -> {
            Map<String, Object> m = new HashMap<>();
            m.put("id", i.getItemId());
            m.put("name", i.getName());
            m.put("price", i.getPrice());
            m.put("thumbnailUrl", "/img/products/" + i.getItemId() + ".jpg");
            m.put("slug", i.getItemId());
            return m;
        }).toList();

        return Map.of("items", items);
    }

    // ================== PRODUCT LIST ==================
    @GetMapping("/product/list")
    public String productList(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String categoryId,
            @RequestParam(required = false) String branchId,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) String sort,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "9") int size,
            Authentication auth,
            HttpSession session,
            Model model) {

        // tên user cho header
        model.addAttribute("userName", resolveDisplayName(auth));

        // ✅ Lưu hoặc lấy branchId từ session
        if (branchId != null && !branchId.isBlank()) {
            session.setAttribute("SELECTED_BRANCH_ID", branchId);
        } else {
            branchId = (String) session.getAttribute("SELECTED_BRANCH_ID");
        }

        // ✅ fallback branch đầu tiên nếu session vẫn null
        if (branchId == null || branchId.isBlank()) {
            branchId = branchRepository.findAll().stream()
                    .findFirst()
                    .map(Branch::getBranchId)
                    .orElse(null);
        }

        List<Category> categories = categoryRepository.findAll().stream()
                .filter(c -> Boolean.TRUE.equals(c.getIsActive()))
                .toList();

        List<Branch> branches = branchRepository.findAll().stream()
                .filter(b -> Boolean.TRUE.equals(b.getIsActive()))
                .toList();

        size = Math.max(size, 1);
        Pageable pageable = PageRequest.of(Math.max(page, 0), size);

        Page<Item> itemsPage;
        if (categoryId != null && !categoryId.isEmpty()) {
            itemsPage = itemRepository.findActiveByCategoryId(categoryId, pageable);
        } else if ("best".equals(sort)) {
            itemsPage = itemRepository.findActiveOrderBySalesDesc(pageable);
        } else if ("new".equals(sort)) {
            itemsPage = itemRepository.findActiveOrderByCreatedAtDesc(pageable);
        } else if (q != null && !q.isEmpty()) {
            List<Item> searchResults = itemRepository.findByNameContainingIgnoreCaseAndIsActiveTrue(q);
            itemsPage = new PageImpl<>(searchResults, pageable, searchResults.size());
        } else {
            itemsPage = itemRepository.findByIsActiveTrue(pageable);
        }

        // ✅ Filter theo giá
        List<Item> filteredItems = itemsPage.getContent();
        if (minPrice != null || maxPrice != null) {
            filteredItems = filteredItems.stream()
                    .filter(item -> {
                        BigDecimal price = item.getPrice();
                        if (price == null)
                            return false;
                        if (minPrice != null && price.compareTo(minPrice) < 0)
                            return false;
                        if (maxPrice != null && price.compareTo(maxPrice) > 0)
                            return false;
                        return true;
                    })
                    .toList();
        }

        // ✅ Sort theo giá
        if ("price_asc".equals(sort)) {
            filteredItems = filteredItems.stream()
                    .sorted(Comparator.comparing(Item::getPrice,
                            Comparator.nullsLast(Comparator.naturalOrder())))
                    .toList();
        } else if ("price_desc".equals(sort)) {
            filteredItems = filteredItems.stream()
                    .sorted(Comparator.comparing(Item::getPrice,
                            Comparator.nullsLast(Comparator.reverseOrder())))
                    .toList();
        }

        // ✅ Map kết quả
        List<Map<String, Object>> productMaps = filteredItems.stream()
                .map(item -> {
                    Map<String, Object> p = new HashMap<>();
                    p.put("id", item.getItemId());
                    p.put("name", item.getName());
                    p.put("price", item.getPrice());
                    p.put("priceText", item.getPrice() != null
                            ? String.format("%,dđ", item.getPrice().longValue())
                            : "—");
                    p.put("thumbnailUrl", "/img/products/" + item.getItemId() + ".jpg");
                    p.put("slug", item.getItemCode() != null ? item.getItemCode() : item.getItemId());
                    return p;
                })
                .toList();

        Page<Map<String, Object>> resultPage = new PageImpl<>(productMaps, pageable, itemsPage.getTotalElements());

        // ✅ Gửi dữ liệu ra view
        model.addAttribute("page", resultPage);
        model.addAttribute("categories", categories);
        model.addAttribute("branches", branches);
        model.addAttribute("branchId", branchId);
        model.addAttribute("q", q);
        model.addAttribute("categoryId", categoryId);
        model.addAttribute("minPrice", minPrice);
        model.addAttribute("maxPrice", maxPrice);
        model.addAttribute("sort", sort);
        model.addAttribute("size", size);

        return "user/product/list";
    }

    // ================== PRODUCT DETAIL ==================
    @GetMapping("/product/detail/{idOrCode}")
    public String productDetail(@PathVariable String idOrCode,
            @RequestParam(required = false) String branchId,
            Authentication auth,
            HttpSession session,
            Model model) {

        model.addAttribute("userName", resolveDisplayName(auth));

        Item item = tryFindItem(idOrCode).orElse(null);
        if (item == null || !Boolean.TRUE.equals(item.getIsActive())) {
            return "redirect:/user/home";
        }

        // ✅ Lấy branchId từ query hoặc session
        String selectedBranchId = (branchId != null && !branchId.isBlank())
                ? branchId
                : (String) session.getAttribute("SELECTED_BRANCH_ID");

        // ✅ Fallback branch đầu tiên nếu không có
        if (selectedBranchId == null || selectedBranchId.isBlank()) {
            selectedBranchId = branchRepository.findAll().stream()
                    .findFirst()
                    .map(Branch::getBranchId)
                    .orElse(null);
        }

        BigDecimal finalPrice = item.getPrice();
        boolean available = Boolean.TRUE.equals(item.getIsActive());
        int stockQty = 0;

        // ✅ Tính giá & tồn kho theo branch
        if (selectedBranchId != null) {
            Branch branch = branchRepository.findById(selectedBranchId).orElse(null);
            if (branch != null) {
                var bipOpt = branchItemPriceRepository.findByItemAndBranch(item, branch);
                if (bipOpt.isPresent()) {
                    BranchItemPrice bip = bipOpt.get();
                    if (bip.getPrice() != null)
                        finalPrice = bip.getPrice();
                    if (bip.getIsAvailable() != null)
                        available = bip.getIsAvailable();
                }

                var invOpt = inventoryRepository
                        .findByBranch_BranchIdAndItem_ItemId(selectedBranchId, item.getItemId());
                if (invOpt.isPresent() && invOpt.get().getQuantity() != null) {
                    stockQty = invOpt.get().getQuantity();
                    available = available && stockQty > 0;
                }
            }
        }

        // ✅ Debug tồn kho
        System.out.println("============== DEBUG PRODUCT DETAIL ==============");
        System.out.println("Branch ID: " + selectedBranchId);
        System.out.println("Item ID: " + item.getItemId());
        var invCheck = inventoryRepository.findByBranch_BranchIdAndItem_ItemId(selectedBranchId, item.getItemId());
        System.out.println("Inventory exists? " + invCheck.isPresent());
        invCheck.ifPresent(inv -> System.out.println("Quantity in DB: " + inv.getQuantity()));
        System.out.println("StockQty being sent: " + stockQty);
        System.out.println("==================================================");

        // ✅ Gallery + review giữ nguyên
        List<ItemMedia> media = itemMediaRepository.findByItem_ItemIdOrderBySortOrderAsc(item.getItemId());
        List<Map<String, String>> gallery = media.stream()
                .filter(m -> m.getMediaUrl() != null && !m.getMediaUrl().isBlank())
                .map(m -> Map.of("url", m.getMediaUrl(), "thumbnailUrl", m.getMediaUrl()))
                .collect(Collectors.toList());

        String mainImageUrl = !gallery.isEmpty()
                ? gallery.get(0).get("url")
                : "/img/products/" + item.getItemId() + ".jpg";

        List<Review> reviews = reviewRepository.findByItemIdOrderByCreatedAtDesc(item.getItemId());
        List<Map<String, Object>> reviewVMs = new ArrayList<>();
        for (Review r : reviews) {
            List<ReviewMedia> rms = reviewMediaRepository.findByReview_ReviewId(r.getReviewId());
            List<Map<String, String>> rmedVM = rms.stream()
                    .map(x -> Map.of("url", x.getMediaUrl(), "thumbnailUrl", x.getMediaUrl()))
                    .collect(Collectors.toList());

            Map<String, Object> rv = new HashMap<>();
            rv.put("userName", maskUser(r.getUserId()));
            rv.put("rating", r.getRating());
            rv.put("content", r.getComment());
            rv.put("createdAt", r.getCreatedAt());
            rv.put("media", rmedVM);
            reviewVMs.add(rv);
        }

        Map<String, Object> product = new HashMap<>();
        product.put("id", item.getItemId());
        product.put("name", item.getName());
        product.put("price", finalPrice);
        product.put("priceText", formatVnCurrency(finalPrice));
        product.put("shortDesc", item.getDescription());
        product.put("longDesc", item.getDescription());
        product.put("mainImageUrl", mainImageUrl);
        product.put("gallery", gallery);
        product.put("available", available);
        product.put("stockQty", stockQty);
        product.put("isBestSeller", false);
        product.put("isNew", false);
        product.put("isFavorite", false);
        product.put("rating", calcAverage(reviews));
        product.put("reviewCount", reviews.size());
        product.put("nutrition", DEFAULT_NUTRITION);

        List<Map<String, Object>> related;
        if (item.getCategory() != null) {
            related = itemRepository
                    .findActiveByCategoryId(item.getCategory().getCategoryId(), PageRequest.of(0, 4))
                    .stream()
                    .filter(i -> !Objects.equals(i.getItemId(), item.getItemId()))
                    .map(this::mapRelated)
                    .collect(Collectors.toList());
        } else {
            related = itemRepository.findByIsActiveTrue(PageRequest.of(0, 4))
                    .stream()
                    .filter(i -> !Objects.equals(i.getItemId(), item.getItemId()))
                    .map(this::mapRelated)
                    .collect(Collectors.toList());
        }

        model.addAttribute("product", product);
        model.addAttribute("reviews", reviewVMs);
        model.addAttribute("related", related);
        model.addAttribute("branchId", selectedBranchId); // ✅ gửi xuống view

        return "user/product/detail";
    }

    private Map<String, Object> mapRelated(Item i) {
        Map<String, Object> m = new HashMap<>();
        m.put("id", i.getItemId());
        m.put("name", i.getName());
        m.put("price", i.getPrice());
        m.put("priceText", formatVnCurrency(i.getPrice()));
        m.put("thumbnailUrl", "/img/products/" + i.getItemId() + ".jpg");
        m.put("slug", i.getItemCode());
        return m;
    }

    private Optional<Item> tryFindItem(String idOrCode) {
        if (idOrCode != null && idOrCode.length() == 36) {
            var byId = itemRepository.findById(idOrCode);
            if (byId.isPresent())
                return byId;
        }
        return itemRepository.findByItemCode(idOrCode);
    }

    private String maskUser(String userId) {
        if (userId == null || userId.length() < 4)
            return "User";
        return "User-" + userId.substring(0, 4) + "****";
    }

    private Double calcAverage(List<Review> reviews) {
        if (reviews == null || reviews.isEmpty())
            return null;
        return reviews.stream()
                .filter(r -> r.getRating() != null)
                .mapToInt(Review::getRating)
                .average()
                .orElse(Double.NaN);
    }

    private String formatVnCurrency(BigDecimal value) {
        if (value == null)
            return "—";
        DecimalFormatSymbols sym = new DecimalFormatSymbols();
        sym.setGroupingSeparator('.');
        sym.setDecimalSeparator(',');
        DecimalFormat df = new DecimalFormat("#,##0", sym);
        return df.format(value) + "đ";
    }

    // ===== BRANCH LIST =====
    // ==== LIST BRANCHES (giữ nguyên) ====
    @GetMapping("/branches")
    public String listBranches(@RequestParam(value = "q", required = false) String q,
            @RequestParam(value = "city", required = false) String city,
            @RequestParam(value = "status", required = false) String status,
            Authentication auth,
            HttpSession session,
            Model model) {

        model.addAttribute("userName", resolveDisplayName(auth));
        String defaultBranchId = ensureDefaultBranchInSession(session);

        List<Branch> all = branchRepository.findAll();

        List<Branch> filtered = all.stream()
                .filter(b -> Boolean.TRUE.equals(b.getIsActive()))
                .filter(b -> {
                    if (q == null || q.isBlank())
                        return true;
                    String name = Optional.ofNullable(b.getName()).orElse("");
                    String addr = Optional.ofNullable(b.getAddress()).orElse("");
                    String ql = q.toLowerCase();
                    return name.toLowerCase().contains(ql) || addr.toLowerCase().contains(ql);
                })
                .filter(b -> {
                    if (city == null || city.isBlank())
                        return true;
                    String bc = b.getCity();
                    return bc != null && bc.equalsIgnoreCase(city);
                })
                .toList();

        if (status != null && !status.isBlank()) {
            if (status.equalsIgnoreCase("OPEN")) {
                filtered = filtered.stream().filter(b -> Boolean.TRUE.equals(b.getIsActive())).toList();
            } else if (status.equalsIgnoreCase("CLOSED")) {
                filtered = all.stream().filter(b -> !Boolean.TRUE.equals(b.getIsActive())).toList();
            }
        }

        List<String> cities = all.stream()
                .map(Branch::getCity)
                .filter(c -> c != null && !c.isBlank())
                .distinct()
                .toList();

        List<BranchListDTO> vms = filtered.stream()
                .map(b -> BranchListDTO.builder()
                        .branchId(b.getBranchId())
                        .name(b.getName())
                        .address(b.getAddress())
                        .phone(b.getPhone())
                        .isActive(Boolean.TRUE.equals(b.getIsActive()))
                        .openHours(b.getOpenHours() != null ? b.getOpenHours() : "08:00 - 22:00")
                        .isDefault(defaultBranchId != null && defaultBranchId.equals(b.getBranchId()))
                        .build())
                .toList();

        List<Map<String, Object>> mapPoints = filtered.stream()
                .filter(b -> b.getLatitude() != null && b.getLongitude() != null)
                .map(b -> {
                    Map<String, Object> m = new HashMap<>();
                    m.put("id", b.getBranchId());
                    m.put("name", b.getName());
                    m.put("addr", b.getAddress());
                    m.put("lat", b.getLatitude().doubleValue());
                    m.put("lng", b.getLongitude().doubleValue());
                    m.put("isActive", Boolean.TRUE.equals(b.getIsActive()));
                    m.put("openHours", b.getOpenHours());
                    return m;
                })
                .toList();

        model.addAttribute("mapPoints", mapPoints);
        model.addAttribute("branches", vms);
        model.addAttribute("cities", cities);
        model.addAttribute("q", q);
        model.addAttribute("city", city);
        model.addAttribute("status", status);

        return "user/branch/list";
    }

    // ==== LEGACY REDIRECT ====
    @GetMapping("/branch/list")
    public String legacyBranchListRedirect() {
        return "redirect:/user/branches";
    }

    // ==== FORM (non-AJAX) → REDIRECT ====
    @PostMapping("/branch/{id}/set-default")
    public String setDefaultBranchForm(@PathVariable("id") String id,
            HttpSession session,
            RedirectAttributes ra,
            jakarta.servlet.http.HttpServletRequest req) {
        session.setAttribute("DEFAULT_BRANCH_ID", id);
        ra.addFlashAttribute("toastSuccess", "Đã chọn chi nhánh mặc định!");
        String ref = req.getHeader("Referer");
        return "redirect:" + ((ref != null && !ref.isBlank()) ? ref : "/user/branches");
    }

    // ==== AJAX (X-Requested-With=XMLHttpRequest) → JSON ====
    @PostMapping(value = "/branch/{id}/set-default", headers = "X-Requested-With=XMLHttpRequest")
    @org.springframework.web.bind.annotation.ResponseBody
    public Map<String, Object> setDefaultBranchAjax(@PathVariable("id") String id,
            HttpSession session) {
        session.setAttribute("DEFAULT_BRANCH_ID", id);
        return java.util.Map.of("ok", true, "defaultBranchId", id);
    }

    /**
     * Đảm bảo có DEFAULT_BRANCH_ID trong session. Nếu chưa có -> set ID chi nhánh
     * active đầu tiên.
     */
    private String ensureDefaultBranchInSession(HttpSession session) {
        String cur = (String) session.getAttribute("DEFAULT_BRANCH_ID");
        if (cur != null && !cur.isBlank())
            return cur;

        String firstActiveId = branchRepository.findAll().stream()
                .filter(b -> Boolean.TRUE.equals(b.getIsActive()))
                .map(Branch::getBranchId)
                .findFirst()
                .orElse(null);

        if (firstActiveId != null) {
            session.setAttribute("DEFAULT_BRANCH_ID", firstActiveId);
            return firstActiveId;
        }
        return null;
    }

    /* ===== ADDRESS CRUD ===== */
    @GetMapping("/addresses")
    public String addressesPage(Model model, Authentication auth) {
        if (auth == null || !auth.isAuthenticated()) {
            return "redirect:/login";
        }

        // tên user cho header
        model.addAttribute("userName", resolveDisplayName(auth));

        String userKey = auth.getName();
        List<AddressDTO> list = addressService.getAddressesForUser(userKey);

        // form rỗng cho chế độ tạo mới
        AddressFormDTO emptyForm = new AddressFormDTO();
        emptyForm.setId(null);
        emptyForm.setFullName("");
        emptyForm.setPhone("");
        emptyForm.setLine("");
        emptyForm.setDistrict("");
        emptyForm.setCity("");
        emptyForm.setIsDefault(false);

        model.addAttribute("addresses", list);
        model.addAttribute("formMode", "create");
        model.addAttribute("addressForm", emptyForm);
        model.addAttribute("error", null);

        return "user/address/manage";
    }

    /**
     * GET /user/addresses/{id}/edit
     * - Hiển thị lại cùng template nhưng formMode = "edit"
     * - Fill form bằng địa chỉ có sẵn
     */
    @GetMapping("/addresses/{id}/edit")
    public String editAddressPage(
            @PathVariable("id") Integer id,
            Model model,
            Authentication auth) {
        if (auth == null || !auth.isAuthenticated()) {
            return "redirect:/login";
        }

        // tên user cho header
        model.addAttribute("userName", resolveDisplayName(auth));

        String userKey = auth.getName();
        List<AddressDTO> list = addressService.getAddressesForUser(userKey);

        // tìm địa chỉ cần edit trong list userKey
        AddressDTO target = list.stream()
                .filter(a -> Objects.equals(a.getId(), id))
                .findFirst()
                .orElse(null);

        if (target == null) {
            // Không tìm thấy -> quay lại template nhưng báo lỗi
            AddressFormDTO emptyForm = new AddressFormDTO();
            emptyForm.setId(null);
            emptyForm.setFullName("");
            emptyForm.setPhone("");
            emptyForm.setLine("");
            emptyForm.setDistrict("");
            emptyForm.setCity("");
            emptyForm.setIsDefault(false);

            model.addAttribute("addresses", list);
            model.addAttribute("formMode", "create");
            model.addAttribute("addressForm", emptyForm);
            model.addAttribute("error", "Không tìm thấy địa chỉ.");

            return "user/address/manage";
        }

        // map DTO -> form để show lên input
        AddressFormDTO form = new AddressFormDTO();
        form.setId(target.getId());
        form.setFullName(target.getFullName());
        form.setPhone(target.getPhone());
        form.setLine(target.getLine());
        form.setDistrict(target.getDistrict());
        form.setCity(target.getCity());
        form.setIsDefault(target.isDefault());

        model.addAttribute("addresses", list);
        model.addAttribute("formMode", "edit");
        model.addAttribute("addressForm", form);
        model.addAttribute("error", null);

        return "user/address/manage";
    }

    /**
     * POST /user/addresses/create
     * - Tạo địa chỉ mới
     */
    @PostMapping("/addresses/create")
    public String createAddress(
            @ModelAttribute AddressFormDTO addressForm,
            Authentication auth,
            org.springframework.web.servlet.mvc.support.RedirectAttributes ra) {
        if (auth == null || !auth.isAuthenticated()) {
            return "redirect:/login";
        }

        String userKey = auth.getName();

        try {
            addressService.createAddress(userKey, addressForm);
            ra.addFlashAttribute("toastSuccess", "Đã thêm địa chỉ!");
        } catch (RuntimeException ex) {
            ra.addFlashAttribute("toastError", ex.getMessage());
        }

        return "redirect:/user/addresses";
    }

    /**
     * POST /user/addresses/update
     * - Cập nhật địa chỉ
     */
    @PostMapping("/addresses/update")
    public String updateAddress(
            @ModelAttribute AddressFormDTO addressForm,
            Authentication auth,
            org.springframework.web.servlet.mvc.support.RedirectAttributes ra) {
        if (auth == null || !auth.isAuthenticated()) {
            return "redirect:/login";
        }

        String userKey = auth.getName();

        if (addressForm.getId() == null) {
            ra.addFlashAttribute("toastError", "Thiếu ID địa chỉ để cập nhật.");
            return "redirect:/user/addresses";
        }

        try {
            addressService.updateAddress(userKey, addressForm);
            ra.addFlashAttribute("toastSuccess", "Đã lưu thay đổi!");
        } catch (RuntimeException ex) {
            ra.addFlashAttribute("toastError", ex.getMessage());
        }

        return "redirect:/user/addresses";
    }

    /**
     * POST /user/addresses/{id}/delete
     * - Xoá địa chỉ cụ thể
     */
    @PostMapping("/addresses/{id}/delete")
    public String deleteAddress(
            @PathVariable("id") Integer id,
            Authentication auth,
            org.springframework.web.servlet.mvc.support.RedirectAttributes ra) {
        if (auth == null || !auth.isAuthenticated()) {
            return "redirect:/login";
        }

        String userKey = auth.getName();

        try {
            addressService.deleteAddress(userKey, id);
            ra.addFlashAttribute("toastSuccess", "Đã xoá địa chỉ!");
        } catch (RuntimeException ex) {
            ra.addFlashAttribute("toastError", ex.getMessage());
        }

        return "redirect:/user/addresses";
    }

    /**
     * POST /user/addresses/{id}/default
     * - Đặt 1 địa chỉ làm mặc định cho user
     */
    @PostMapping("/addresses/{id}/default")
    public String setDefaultAddress(
            @PathVariable("id") Integer id,
            Authentication auth,
            org.springframework.web.servlet.mvc.support.RedirectAttributes ra) {
        if (auth == null || !auth.isAuthenticated()) {
            return "redirect:/login";
        }

        String userKey = auth.getName();

        try {
            addressService.setDefault(userKey, id);
            ra.addFlashAttribute("toastSuccess", "Đã đặt làm mặc định!");
        } catch (RuntimeException ex) {
            ra.addFlashAttribute("toastError", ex.getMessage());
        }

        return "redirect:/user/addresses";
    }

    // ===== CART =====
    @PostMapping("/cart")
    public Object addToCartFromForm(
            @RequestParam(value = "itemId", required = false) String itemId,
            @RequestParam(value = "productId", required = false) String productId,
            @RequestParam(value = "branchId", required = false) String branchId,
            @RequestParam(value = "quantity", required = false) Integer quantity,
            Authentication auth,
            jakarta.servlet.http.HttpServletRequest request,
            jakarta.servlet.http.HttpSession session,
            org.springframework.web.servlet.mvc.support.RedirectAttributes ra) {

        // 1) Bắt login
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getName())) {
            // Nếu là AJAX → trả JSON báo cần login
            if (isAjax(request)) {
                return java.util.Map.of("ok", false, "needLogin", true, "message", "Bạn cần đăng nhập");
            }
            ra.addFlashAttribute("toastError", "Bạn cần đăng nhập để thêm vào giỏ.");
            return "redirect:/auth";
        }

        // 2) Validate input
        String resolvedItemId = (itemId != null && !itemId.isBlank()) ? itemId : productId;
        if (resolvedItemId == null || resolvedItemId.isBlank()) {
            if (isAjax(request)) {
                return java.util.Map.of("ok", false, "message", "Thiếu mã sản phẩm.");
            }
            ra.addFlashAttribute("toastError", "Thiếu mã sản phẩm.");
            return "redirect:/user/cart";
        }

        // 3) Build request + add
        String userIdOrLogin = auth.getName(); // email hoặc userId đều ok với resolveUser(...)
        var req = new com.example.Alotrabong.dto.AddToCartRequest();
        req.setItemId(resolvedItemId);
        req.setBranchId(branchId);
        req.setQuantity(quantity);

        try {
            cartService.addToCart(userIdOrLogin, req);
        } catch (RuntimeException ex) {
            if (isAjax(request)) {
                return java.util.Map.of("ok", false, "message", ex.getMessage());
            }
            ra.addFlashAttribute("toastError", ex.getMessage());
            return redirectBack(request, branchId);
        }

        // 4) Lấy cartCount ngay sau khi add để update header tức thì (cho AJAX)
        int cartCount = cartService.getCartItemCount(userIdOrLogin, branchId);

        // 5) AJAX → trả JSON (frontend cập nhật .cart-badge b)
        if (isAjax(request)) {
            return java.util.Map.of(
                    "ok", true,
                    "message", "Đã thêm vào giỏ!",
                    "cartCount", cartCount);
        }

        // 6) Form thường → flash + redirect về Referer (ưu tiên) hoặc trang giỏ
        ra.addFlashAttribute("toastSuccess", "Đã thêm vào giỏ!");
        return redirectBack(request, branchId);
    }

    // ==== helpers ====
    private boolean isAjax(jakarta.servlet.http.HttpServletRequest req) {
        String hx = req.getHeader("HX-Request"); // htmx
        String xr = req.getHeader("X-Requested-With"); // ajax cổ điển
        String acc = req.getHeader("Accept"); // fetch JSON
        return "true".equalsIgnoreCase(hx)
                || "XMLHttpRequest".equalsIgnoreCase(xr)
                || (acc != null && acc.contains("application/json"));
    }

    private String redirectBack(jakarta.servlet.http.HttpServletRequest req, String branchId) {
        String referer = req.getHeader("Referer");
        if (referer != null && !referer.isBlank()) {
            return "redirect:" + referer;
        }
        String q = (branchId != null && !branchId.isBlank()) ? "?branchId=" + branchId : "";
        return "redirect:/user/cart" + q;
    }

    @GetMapping("/cart")
    public String cart(@RequestParam(required = false) String branchId,
            Authentication auth,
            Model model) {

        // tên user cho header
        model.addAttribute("userName", resolveDisplayName(auth));

        Branch branch = resolveBranch(branchId);
        String resolvedBranchId = branch != null ? branch.getBranchId() : null;

        String userKey = (auth != null && auth.isAuthenticated())
                ? auth.getName()
                : "user-id-placeholder";

        var dtos = cartService.getCartItems(userKey, resolvedBranchId);
        var items = new ArrayList<Map<String, Object>>();

        for (var d : dtos) {
            var it = new HashMap<String, Object>();
            var item = itemRepository.findById(d.getItemId()).orElse(null);
            String slug = item != null ? item.getItemCode() : d.getItemId();
            String thumb = itemMediaRepository
                    .findByItem_ItemIdOrderBySortOrderAsc(d.getItemId())
                    .stream()
                    .findFirst()
                    .map(ItemMedia::getMediaUrl)
                    .orElse("/img/products/" + d.getItemId() + ".jpg");
            String branchName = branch != null ? branch.getName() : "";

            it.put("cartItemId", d.getCartItemId());
            it.put("itemId", d.getItemId());
            it.put("name", d.getItemName());
            it.put("slug", slug);
            it.put("thumbnailUrl", thumb);
            it.put("branchName", branchName);
            it.put("unitPrice", d.getUnitPrice());
            it.put("qty", d.getQuantity());
            it.put("subtotal", d.getTotalPrice());
            items.add(it);
        }

        BigDecimal subtotal = dtos.stream()
                .map(CartItemDTO::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal discount = BigDecimal.ZERO;
        BigDecimal shipping = new BigDecimal("15000");
        BigDecimal grand = subtotal.add(shipping).subtract(discount);

        var summary = new HashMap<String, BigDecimal>();
        summary.put("subtotal", subtotal);
        summary.put("discount", discount);
        summary.put("shippingFee", shipping);
        summary.put("grandTotal", grand);

        model.addAttribute("items", items);
        model.addAttribute("summary", summary);
        model.addAttribute("branchId", resolvedBranchId);
        model.addAttribute("appliedCoupon", null);
        model.addAttribute("couponError", null);
        return "user/cart/cart";
    }

    // ===== ORDER HISTORY =====
    @GetMapping("/order/history")
    public String orderHistory(
            @RequestParam(required = false) OrderStatus status,
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Authentication auth,
            Model model) {

        if (auth == null || !auth.isAuthenticated()) {
            return "redirect:/login";
        }

        // tên user cho header
        model.addAttribute("userName", resolveDisplayName(auth));

        final String login = auth.getName();
        User user = userRepository.findByLogin(login)
                .orElseGet(() -> userRepository.findById(login).orElse(null));

        if (user == null) {
            model.addAttribute("page", Page.<OrderHistoryVM>empty());
            model.addAttribute("status", status != null ? status.name() : "");
            model.addAttribute("from", from);
            model.addAttribute("to", to);
            model.addAttribute("error", "Không xác định được người dùng.");
            return "user/order/history";
        }

        LocalDate fromDate = safeParseDate(from);
        LocalDate toDate = safeParseDate(to);
        if (fromDate != null && toDate != null && fromDate.isAfter(toDate)) {
            LocalDate tmp = fromDate;
            fromDate = toDate;
            toDate = tmp;
        }

        int safePage = Math.max(page, 0);
        int safeSize = Math.max(5, Math.min(size, 50));

        Page<OrderHistoryVM> vmPage = orderHistoryService.getHistory(
                user,
                status,
                fromDate,
                toDate,
                safePage,
                safeSize);

        model.addAttribute("page", vmPage);
        model.addAttribute("status", status != null ? status.name() : "");
        model.addAttribute("from", from);
        model.addAttribute("to", to);

        return "user/order/history";
    }

    

    @GetMapping("/order/detail/{code}")
    public String orderDetail(
            @PathVariable String code,
            @RequestParam(required = false) String action,
            Authentication auth,
            Model model) {

        if (auth == null || !auth.isAuthenticated()) {
            return "redirect:/login";
        }

        model.addAttribute("userName", resolveDisplayName(auth));

        final String login = auth.getName();
        User user = userRepository.findByLogin(login)
                .orElseGet(() -> userRepository.findById(login).orElse(null));

        if (user == null) {
            model.addAttribute("error", "Không xác định được người dùng.");
            return "user/order/detail";
        }

        OrderDetailVM detail = orderHistoryService.getOrderDetailForUser(user, code);
        if (detail == null) {
            model.addAttribute("error", "Không tìm thấy đơn hàng hoặc bạn không có quyền xem.");
            return "user/order/detail";
        }

        // --- KHÔNG DÙNG LAMBDA: lấy order ra rồi patch ngoài ---
        var orderOpt = orderRepository.findById(code);
        if (orderOpt.isPresent()) {
            var order = orderOpt.get();

            BigDecimal ship = order.getShippingFee() == null ? BigDecimal.ZERO : order.getShippingFee();
            BigDecimal dc = order.getDiscount() == null ? BigDecimal.ZERO : order.getDiscount();
            BigDecimal grand = (detail.grandTotal() != null)
                    ? detail.grandTotal()
                    : detail.subtotal().add(ship).subtract(dc);

            // record bất biến -> tạo instance mới
            detail = new OrderDetailVM(
                    detail.code(),
                    detail.status(),
                    detail.createdAt(),
                    detail.branchName(),
                    detail.deliveryAddress(),
                    detail.customerNote(),
                    detail.paymentMethod(),
                    order.getPaymentStatus(),
                    detail.items(),
                    detail.subtotal(),
                    ship,
                    dc,
                    grand,
                    detail.cancellable());
        }

        model.addAttribute("order", detail);

        if ("cancel".equalsIgnoreCase(action)) {
            model.addAttribute("cancelMode", true);
        }

        return "user/order/detail";
    }

    @GetMapping("/orders")
    public String ordersRedirect(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        String qs = org.springframework.web.util.UriComponentsBuilder
                .fromPath("/user/order/history")
                .queryParam("status", status == null ? "" : status)
                .queryParam("from", from == null ? "" : from)
                .queryParam("to", to == null ? "" : to)
                .queryParam("page", Math.max(page, 0))
                .queryParam("size", Math.max(5, Math.min(size, 50)))
                .build()
                .toUriString();

        return "redirect:" + qs;
    }

    @GetMapping("/orders/{code}")
    public String ordersDetailRedirect(@PathVariable String code) {
        return "redirect:/user/order/detail/" + code;
    }

    @PostMapping("/order/{code}/cancel")
    public String cancelOrder(
            @PathVariable String code,
            Authentication auth,
            Model model) {

        if (auth == null || !auth.isAuthenticated()) {
            return "redirect:/login";
        }

        final String login = auth.getName();
        User user = userRepository.findByLogin(login)
                .orElseGet(() -> userRepository.findById(login).orElse(null));

        if (user == null) {
            model.addAttribute("error", "Không xác định được người dùng.");
            return "redirect:/user/order/detail/" + code;
        }

        boolean ok = orderHistoryService.cancelOrderForUser(user, code);
        // bạn có thể check 'ok' để flash message
        return "redirect:/user/order/detail/" + code;
    }

    /* ===================== Helpers private ===================== */

    private LocalDate safeParseDate(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        try {
            return LocalDate.parse(raw);
        } catch (Exception ignore) {
            return null;
        }
    }

    @GetMapping("/address/manage")
    public String addressManage() {
        // route legacy cũ, nếu UI bạn vẫn gọi /user/address/manage có thể redirect
        return "redirect:/user/addresses";
    }

    // ===== FAVORITE (session-based) =====
    @SuppressWarnings("unchecked")
    private Set<String> getFavoriteSet(HttpSession session) {
        Object fv = session.getAttribute("FAVORITES");
        if (fv instanceof Set<?>)
            return (Set<String>) fv;
        Set<String> s = new HashSet<>();
        session.setAttribute("FAVORITES", s);
        return s;
    }

    @PostMapping("/favorite/add")
    public String addFavorite(@RequestParam("productId") String itemId,
            Authentication auth,
            RedirectAttributes ra) {

        if (auth == null || !auth.isAuthenticated()) {
            ra.addFlashAttribute("toastError", "Bạn cần đăng nhập để thêm yêu thích.");
            return "redirect:/login";
        }

        var user = userRepository.findByEmail(auth.getName()).orElse(null);
        if (user == null) {
            ra.addFlashAttribute("toastError", "Không tìm thấy người dùng.");
            return "redirect:/user/favorite/list";
        }

        var item = itemRepository.findById(itemId).orElse(null);
        if (item == null) {
            ra.addFlashAttribute("toastError", "Món không tồn tại.");
            return "redirect:/user/favorite/list";
        }

        var existing = favoriteRepo.findByUser_UserIdAndItem_ItemId(user.getUserId(), itemId);
        if (existing.isPresent()) {
            ra.addFlashAttribute("toastError", "Món đã có trong yêu thích rồi!");
        } else {
            var fav = Favorite.builder()
                    .user(user)
                    .item(item)
                    .build();
            favoriteRepo.save(fav);
            ra.addFlashAttribute("toastSuccess", "Đã thêm vào yêu thích!");
        }

        return "redirect:/user/favorite/list";
    }

    @PostMapping("/favorite/{id}/remove")
    public String removeFavorite(@PathVariable("id") String itemId,
            Authentication auth,
            RedirectAttributes ra) {

        if (auth == null || !auth.isAuthenticated()) {
            ra.addFlashAttribute("toastError", "Bạn cần đăng nhập để xóa yêu thích.");
            return "redirect:/login";
        }

        var user = userRepository.findByEmail(auth.getName()).orElse(null);
        if (user == null) {
            ra.addFlashAttribute("toastError", "Không tìm thấy người dùng.");
            return "redirect:/user/favorite/list";
        }

        var deletedCount = favoriteRepo.deleteByUser_UserIdAndItem_ItemId(user.getUserId(), itemId);
        if (deletedCount > 0) {
            ra.addFlashAttribute("toastSuccess", "Đã bỏ khỏi yêu thích!");
        } else {
            ra.addFlashAttribute("toastError", "Món này không nằm trong danh sách yêu thích.");
        }

        return "redirect:/user/favorite/list";
    }

    @PostMapping("/favorite/toggle")
    public String toggleFavorite(@RequestParam("productId") String itemId,
            Authentication auth,
            RedirectAttributes ra) {

        if (auth == null || !auth.isAuthenticated()) {
            ra.addFlashAttribute("toastError", "Bạn cần đăng nhập để thao tác yêu thích.");
            return "redirect:/login";
        }

        var user = userRepository.findByEmail(auth.getName()).orElse(null);
        if (user == null) {
            ra.addFlashAttribute("toastError", "Không tìm thấy người dùng.");
            return "redirect:/user/favorite/list";
        }

        var item = itemRepository.findById(itemId).orElse(null);
        if (item == null) {
            ra.addFlashAttribute("toastError", "Món không tồn tại.");
            return "redirect:/user/favorite/list";
        }

        var existing = favoriteRepo.findByUser_UserIdAndItem_ItemId(user.getUserId(), itemId);
        if (existing.isPresent()) {
            favoriteRepo.deleteByUser_UserIdAndItem_ItemId(user.getUserId(), itemId);
            ra.addFlashAttribute("toastSuccess", "Đã bỏ khỏi yêu thích!");
        } else {
            favoriteRepo.save(Favorite.builder().user(user).item(item).build());
            ra.addFlashAttribute("toastSuccess", "Đã thêm vào yêu thích!");
        }

        return "redirect:/user/favorite/list";
    }

    @GetMapping("/favorite/list")
    public String favoriteList(Model model, Authentication auth) {

        if (auth == null || !auth.isAuthenticated()) {
            return "redirect:/login";
        }

        // tên user cho header
        model.addAttribute("userName", resolveDisplayName(auth));

        var user = userRepository.findByEmail(auth.getName()).orElse(null);
        if (user == null) {
            model.addAttribute("items", java.util.Collections.emptyList());
            model.addAttribute("page", null);
            return "user/favorite/list";
        }

        var favorites = favoriteRepo.findByUser_UserId(user.getUserId());
        var items = favorites.stream().map(f -> {
            var i = f.getItem();
            var thumb = itemMediaRepository
                    .findByItem_ItemIdOrderBySortOrderAsc(i.getItemId())
                    .stream()
                    .findFirst()
                    .map(media -> media.getMediaUrl())
                    .orElse("/img/placeholder-product.jpg");

            var m = new java.util.HashMap<String, Object>();
            m.put("id", i.getItemId());
            m.put("name", i.getName());
            m.put("price", i.getPrice());
            m.put("thumbnailUrl", thumb);
            m.put("slug", i.getItemCode());
            return m;
        }).toList();

        model.addAttribute("items", items);
        model.addAttribute("page", null);
        return "user/favorite/list";
    }

    // ===== COUPON =====
    @GetMapping("/coupon/list")
    public String couponList(Model model, HttpSession session, Authentication auth) {

        // tên user cho header
        model.addAttribute("userName", resolveDisplayName(auth));

        var now = java.time.LocalDateTime.now();
        var coupons = couponRepository.findByIsActiveTrue()
                .stream()
                .filter(c -> c.getEndDate() == null || c.getEndDate().isAfter(now))
                .toList();

        String appliedCode = (String) session.getAttribute("appliedCouponCode");
        Map<String, Object> applied = null;
        if (appliedCode != null) {
            applied = Map.of("code", appliedCode);
        }

        model.addAttribute("coupons", coupons);
        model.addAttribute("applied", applied);
        model.addAttribute("error", session.getAttribute("couponError"));

        session.removeAttribute("couponError");
        return "user/coupon/list";
    }

    @PostMapping("/coupons/apply")
    public String applyCoupon(@RequestParam("code") String code,
            HttpSession session,
            org.springframework.web.servlet.mvc.support.RedirectAttributes ra) {

        if (code == null || code.isBlank()) {
            session.setAttribute("couponError", "Vui long nhap ma giam gia.");
            return "redirect:/user/coupon/list";
        }

        var couponOpt = couponRepository.findByCodeAndIsActiveTrue(code.trim());
        if (couponOpt.isEmpty()) {
            session.setAttribute("couponError", "Ma giam gia khong hop le hoac het han.");
            return "redirect:/user/coupon/list";
        }

        var coupon = couponOpt.get();
        var now = java.time.LocalDateTime.now();
        if (coupon.getEndDate() != null && coupon.getEndDate().isBefore(now)) {
            session.setAttribute("couponError", "Ma nay da het han.");
            return "redirect:/user/coupon/list";
        }

        session.setAttribute("appliedCouponCode", coupon.getCode());
        session.removeAttribute("couponError");
        ra.addFlashAttribute("toastSuccess", "Da ap dung ma: " + coupon.getCode());
        return "redirect:/user/coupon/list";
    }

    @GetMapping("/review/write")
    public String reviewWrite(@RequestParam("itemId") String itemIdOrCode,
            @RequestParam(required = false) String orderCode,
            @RequestParam(required = false) String backUrl,
            @RequestHeader(value = "Referer", required = false) String referer,
            HttpServletRequest request,
            Authentication auth,
            Model model) {

        // tên user cho header
        model.addAttribute("userName", resolveDisplayName(auth));

        Item item = tryFindItem(itemIdOrCode).orElse(null);
        if (item == null || !Boolean.TRUE.equals(item.getIsActive())) {
            return "redirect:/user/home";
        }

        List<ItemMedia> media = itemMediaRepository.findByItem_ItemIdOrderBySortOrderAsc(item.getItemId());

        String thumb = (media != null && !media.isEmpty() && media.get(0).getMediaUrl() != null)
                ? media.get(0).getMediaUrl()
                : "/img/products/" + item.getItemId() + ".jpg";

        Map<String, Object> productMini = new HashMap<>();
        productMini.put("id", item.getItemId());
        productMini.put("name", item.getName());
        productMini.put("thumbnailUrl", thumb);
        model.addAttribute("product", productMini);

        model.addAttribute("orderCode", orderCode);
        model.addAttribute("deliveredAt", null);

        String safeBack = backUrl;
        if ((safeBack == null || safeBack.isBlank()) && referer != null) {
            try {
                java.net.URI ref = java.net.URI.create(referer);
                String host = request.getServerName();
                if (ref.getHost() == null || ref.getHost().equalsIgnoreCase(host)) {
                    safeBack = ref.getPath() + (ref.getQuery() != null ? "?" + ref.getQuery() : "");
                }
            } catch (Exception ignored) {
            }
        }
        model.addAttribute("backUrl", safeBack);

        return "user/review/write";
    }

    @GetMapping("/chat/room/{roomId}")
    public String chatRoom(@PathVariable String roomId, Authentication auth, Model model) {
        // Lấy thông tin user hiện tại
        User currentUser = userRepository.findByEmail(auth.getName())
                .orElse(null);
        if (currentUser == null) {
            return "redirect:/login";
        }

        // Tên user cho header
        model.addAttribute("userName", resolveDisplayName(auth));
        // User ID để phân biệt tin nhắn
        model.addAttribute("currentUserId", currentUser.getUserId());
        // Branch ID từ roomId (có thể là branchId)
        model.addAttribute("branchId", roomId);

        // Lấy thông tin branch
        Branch branch = resolveBranch(roomId);
        if (branch != null) {
            model.addAttribute("branchName", branch.getName());
        }

        return "user/chat/room";
    }

    @GetMapping("/dashboard")
    public String dashboardFallback() {
        return "redirect:/user/home";
    }

    // ===== helpers =====
    private Branch resolveBranch(String branchIdMaybe) {
        if (branchIdMaybe != null && !branchIdMaybe.isBlank()) {
            Optional<Branch> byId = branchRepository.findById(branchIdMaybe);
            if (byId.isPresent())
                return byId.get();
        }
        return fallbackActiveBranch();
    }

    private Branch fallbackActiveBranch() {
        return branchRepository.findAll().stream()
                .filter(b -> Boolean.TRUE.equals(b.getIsActive()))
                .findFirst()
                .orElse(null);
    }

    // legacy redirect /checkout -> /user/checkout
    @Controller("legacyRedirectController")
    @PreAuthorize("permitAll()")
    static class UserLegacyRedirects {
        @GetMapping("/checkout")
        public String redirectCheckoutLegacy() {
            return "redirect:/user/checkout";
        }
    }

    @PostMapping("/review/submit")
    public String submitReview(
            @RequestParam("productId") String productId,
            @RequestParam(value = "orderCode", required = false) String orderCode,
            @RequestParam("rating") Integer rating,
            @RequestParam("content") String content,
            @RequestParam(value = "media", required = false) List<MultipartFile> media,
            Authentication auth,
            org.springframework.web.servlet.mvc.support.RedirectAttributes ra) {

        if (productId == null || productId.isBlank()) {
            ra.addFlashAttribute("error", "Thiếu mã sản phẩm.");
            return "redirect:/user/review/write?itemId=" + productId;
        }
        if (rating == null || rating < 1 || rating > 5) {
            ra.addFlashAttribute("error", "Điểm phải từ 1 đến 5.");
            return "redirect:/user/review/write?itemId=" + productId
                    + (orderCode != null ? "&orderCode=" + orderCode : "");
        }
        if (content == null || content.trim().length() < 50) {
            ra.addFlashAttribute("error", "Nội dung tối thiểu 50 ký tự.");
            return "redirect:/user/review/write?itemId=" + productId
                    + (orderCode != null ? "&orderCode=" + orderCode : "");
        }

        var itemOpt = itemRepository.findById(productId);
        if (itemOpt.isEmpty()) {
            ra.addFlashAttribute("error", "Sản phẩm không tồn tại.");
            return "redirect:/user/home";
        }
        var item = itemOpt.get();

        Review r = new Review();
        r.setItemId(item.getItemId());
        r.setUserId((auth != null && auth.isAuthenticated()) ? auth.getName() : "guest");
        r.setRating(rating);
        r.setComment(content);
        try {
            r.getClass().getMethod("setOrderCode", String.class).invoke(r, orderCode);
        } catch (Exception ignore) {
        }
        r.setCreatedAt(LocalDateTime.now());
        r = reviewRepository.save(r);

        if (media != null && !media.isEmpty()) {
            Path base = Paths.get("uploads", "reviews", r.getReviewId());
            try {
                Files.createDirectories(base);
            } catch (Exception ignore) {
            }
            for (MultipartFile file : media) {
                if (file == null || file.isEmpty())
                    continue;
                String cleanName = file.getOriginalFilename() == null ? "media"
                        : file.getOriginalFilename().replaceAll("[^a-zA-Z0-9._-]", "_");
                Path dest = base.resolve(cleanName);
                try (var in = file.getInputStream()) {
                    Files.copy(in, dest, StandardCopyOption.REPLACE_EXISTING);
                    ReviewMedia rm = new ReviewMedia();
                    rm.setReview(r);
                    rm.setMediaUrl("/uploads/reviews/" + r.getReviewId() + "/" + cleanName);
                    reviewMediaRepository.save(rm);
                } catch (Exception ex) {
                    // swallow lỗi upload file, vẫn cho review ok
                }
            }
        }

        ra.addFlashAttribute("success", "Cảm ơn bạn đã đánh giá ❤️");

        String slug = item.getItemCode() != null ? item.getItemCode() : item.getItemId();
        return "redirect:/user/product/detail/" + slug;
    }
}