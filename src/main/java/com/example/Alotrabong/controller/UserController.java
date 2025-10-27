package com.example.Alotrabong.controller;

import com.example.Alotrabong.dto.CartItemDTO;
import com.example.Alotrabong.dto.HomeItemVM;
import com.example.Alotrabong.dto.ItemDTO;
import com.example.Alotrabong.dto.BranchListDTO;
import com.example.Alotrabong.entity.*;
import com.example.Alotrabong.repository.*;
import com.example.Alotrabong.service.CartService;
import com.example.Alotrabong.service.ItemService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/user")
@PreAuthorize("hasRole('USER')")
@RequiredArgsConstructor
public class UserController {

    // ===== Services =====
    private final ItemService itemService;
    private final CartService cartService;

    // ===== Repositories =====
    private final ItemRepository itemRepository;
    private final ItemMediaRepository itemMediaRepository;
    private final BranchItemPriceRepository branchItemPriceRepository;
    private final InventoryRepository inventoryRepository;
    private final BranchRepository branchRepository;
    private final ReviewRepository reviewRepository;
    private final ReviewMediaRepository reviewMediaRepository;
    private final CouponRepository couponRepository;

    // ===== HOME =====
    @GetMapping({ "", "/" })
    public String userRoot() {
        return "redirect:/user/home";
    }

    @GetMapping({ "/home", "/home/index" })
    public String home(Model model, Authentication auth) {
        model.addAttribute("userName",
                (auth != null && auth.isAuthenticated()) ? auth.getName() : "Người dùng VIP");

        List<HomeItemVM> productsNew = mapItems(itemService.getNewItems(8));
        List<HomeItemVM> productsBest = mapItems(itemService.getTopSellingItems(8));

        model.addAttribute("productsNew", productsNew);
        model.addAttribute("productsBest", productsBest);
        model.addAttribute("productsFav", productsBest);
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

    // Dinh dưỡng mặc định
    private static final Map<String, Object> DEFAULT_NUTRITION = Map.of(
            "cal", 300, "protein", 6, "fat", 10, "carb", 45, "sodium", 160);

    // ===== PRODUCT LIST / DETAIL =====
    @GetMapping("/product/list")
    public String productList(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String categoryId,
            @RequestParam(required = false) String branchId,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) String sort,
            @RequestParam(defaultValue = "0") int page,
            Model model) {

        // Demo data để UI chạy ngay
        List<Map<String, Object>> sampleProducts = new ArrayList<>();
        for (int i = 1; i <= 8; i++) {
            Map<String, Object> p = new HashMap<>();
            p.put("id", String.valueOf(i));
            p.put("name", "Món ăn số " + i);
            p.put("price", BigDecimal.valueOf(25_000 + i * 5_000L));
            p.put("priceText", String.format("%,dđ", 25_000 + i * 5_000L));
            p.put("thumbnailUrl", "/img/products/" + i + ".jpg");
            p.put("slug", "mon-" + i);
            sampleProducts.add(p);
        }

        List<Map<String, Object>> categories = List.of(
                Map.of("id", "1", "name", "Cơm"),
                Map.of("id", "2", "name", "Mì"),
                Map.of("id", "3", "name", "Tráng miệng"));

        List<Map<String, Object>> branches = List.of(
                Map.of("id", "b1", "name", "AloTraBong - TP.HCM"),
                Map.of("id", "b2", "name", "AloTraBong - Hà Nội"));

        var fakePage = new org.springframework.data.domain.PageImpl<>(
                sampleProducts, org.springframework.data.domain.PageRequest.of(page, 8), 20);

        model.addAttribute("page", fakePage);
        model.addAttribute("categories", categories);
        model.addAttribute("branches", branches);
        model.addAttribute("q", q);
        model.addAttribute("categoryId", categoryId);
        model.addAttribute("branchId", branchId);
        model.addAttribute("minPrice", minPrice);
        model.addAttribute("maxPrice", maxPrice);
        model.addAttribute("sort", sort);

        return "user/product/list";
    }

    @GetMapping("/product/detail/{idOrCode}")
    public String productDetail(@PathVariable String idOrCode,
            @RequestParam(required = false) String branchId,
            Model model) {

        Item item = tryFindItem(idOrCode).orElse(null);
        if (item == null || !Boolean.TRUE.equals(item.getIsActive())) {
            return "redirect:/user/home";
        }

        BigDecimal finalPrice = item.getPrice();
        boolean available = Boolean.TRUE.equals(item.getIsActive());

        if (branchId != null && !branchId.isBlank()) {
            Branch branch = branchRepository.findById(branchId).orElse(null);
            if (branch != null) {
                var bipOpt = branchItemPriceRepository.findByItemAndBranch(item, branch);
                if (bipOpt.isPresent()) {
                    BranchItemPrice bip = bipOpt.get();
                    if (bip.getPrice() != null)
                        finalPrice = bip.getPrice();
                    if (bip.getIsAvailable() != null)
                        available = bip.getIsAvailable();
                }
                var invOpt = inventoryRepository.findByBranch_BranchIdAndItem_ItemId(branchId, item.getItemId());
                if (invOpt.isPresent() && invOpt.get().getQuantity() != null) {
                    available = available && invOpt.get().getQuantity() > 0;
                }
            }
        }

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
                .average().orElse(Double.NaN);
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

    // ===== BRANCH (NEW): trang danh sách chi nhánh cho user =====
    @GetMapping("/branches")
    public String listBranches(@RequestParam(value = "q", required = false) String q,
            @RequestParam(value = "city", required = false) String city,
            @RequestParam(value = "status", required = false) String status,
            HttpSession session,
            Model model) {

        // 1) Lấy danh sách từ DB
        List<Branch> all = branchRepository.findAll();

        // 2) Lọc theo query
        List<Branch> filtered = all.stream()
                .filter(b -> b.getIsActive() != null && b.getIsActive()) // UI người dùng chỉ show active
                .filter(b -> q == null || q.isBlank()
                        || b.getName().toLowerCase().contains(q.toLowerCase())
                        || (b.getAddress() != null && b.getAddress().toLowerCase().contains(q.toLowerCase())))
                .filter(b -> city == null || city.isBlank()
                        || (b.getCity() != null && b.getCity().equalsIgnoreCase(city)))
                .collect(Collectors.toList());

        if (status != null && !status.isBlank()) {
            if (status.equalsIgnoreCase("OPEN")) {
                filtered = filtered.stream().filter(b -> Boolean.TRUE.equals(b.getIsActive()))
                        .collect(Collectors.toList());
            } else if (status.equalsIgnoreCase("CLOSED")) {
                filtered = filtered.stream().filter(b -> !Boolean.TRUE.equals(b.getIsActive()))
                        .collect(Collectors.toList());
            }
        }

        // 3) Thành phố cho filter
        List<String> cities = all.stream()
                .map(Branch::getCity)
                .filter(c -> c != null && !c.isBlank())
                .distinct()
                .collect(Collectors.toList());

        // 4) Map -> BranchListDTO để template có distanceKm/deliveryEtaMin/isDefault
        String defaultBranchId = (String) session.getAttribute("DEFAULT_BRANCH_ID");
        List<BranchListDTO> vms = filtered.stream()
                .map(b -> BranchListDTO.builder()
                        .branchId(b.getBranchId())
                        .name(b.getName())
                        .address(b.getAddress())
                        .phone(b.getPhone())
                        .isActive(Boolean.TRUE.equals(b.getIsActive()))
                        .openHours(b.getOpenHours() != null ? b.getOpenHours() : "08:00 - 22:00")
                        .isDefault(defaultBranchId != null && defaultBranchId.equals(b.getBranchId()))
                        // distanceKm & deliveryEtaMin: để null, sẽ tính sau (Haversine)
                        .build())
                .collect(Collectors.toList());

        // Build data cho bản đồ (không đụng DTO cũ)
        List<Map<String, Object>> mapPoints = filtered.stream()
                .filter(b -> b.getLatitude() != null && b.getLongitude() != null) // chỉ lấy những branch có toạ độ
                .map(b -> {
                    Map<String, Object> m = new HashMap<>();
                    m.put("id", b.getBranchId());
                    m.put("name", b.getName());
                    m.put("addr", b.getAddress());
                    m.put("lat", b.getLatitude().doubleValue()); // BigDecimal -> Double cho JS dễ ăn
                    m.put("lng", b.getLongitude().doubleValue());
                    m.put("isActive", Boolean.TRUE.equals(b.getIsActive()));
                    m.put("openHours", b.getOpenHours());
                    return m;
                })
                .toList();

        // add vào model
        model.addAttribute("mapPoints", mapPoints);

        model.addAttribute("branches", vms);
        model.addAttribute("cities", cities);
        model.addAttribute("q", q);
        model.addAttribute("city", city);
        model.addAttribute("status", status);

        return "user/branch/list";
    }

    // Giữ route cũ để khỏi gãy link cũ -> redirect sang route mới
    @GetMapping("/branch/list")
    public String legacyBranchListRedirect() {
        return "redirect:/user/branches";
    }

    @PostMapping("/branch/{id}/set-default")
    public String setDefaultBranch(@PathVariable("id") String id,
            HttpSession session,
            org.springframework.web.servlet.mvc.support.RedirectAttributes ra) {
        session.setAttribute("DEFAULT_BRANCH_ID", id);
        ra.addFlashAttribute("toastSuccess", "Đã chọn chi nhánh mặc định!");
        return "redirect:/user/branches";
    }

    // ===== ADDRESS (session-based demo – chạy ngay, chưa cần DB) =====
    @Getter
    @Setter
    public static class AddressForm {
        private String id;
        private String fullName;
        private String phone;
        private String line;
        private String ward;
        private String district;
        private String city;
        private Boolean asDefault;
    }

    @Getter
    @Setter
    public static class AddressVM {
        private String id;
        private String fullName;
        private String phone;
        private String line;
        private String ward;
        private String district;
        private String city;
        private boolean isDefault;

        static AddressVM fromForm(AddressForm f) {
            AddressVM v = new AddressVM();
            v.id = f.getId();
            v.fullName = f.getFullName();
            v.phone = f.getPhone();
            v.line = f.getLine();
            v.ward = f.getWard();
            v.district = f.getDistrict();
            v.city = f.getCity();
            v.isDefault = Boolean.TRUE.equals(f.getAsDefault());
            return v;
        }
    }

    @SuppressWarnings("unchecked")
    private List<AddressVM> getAddresses(HttpSession session) {
        Object o = session.getAttribute("ADDRESSES");
        if (o instanceof List<?> list) {
            return (List<AddressVM>) list;
        }
        // seed demo nếu muốn có sẵn 1 địa chỉ
        List<AddressVM> init = new ArrayList<>();
        session.setAttribute("ADDRESSES", init);
        return init;
    }

    private void setDefault(List<AddressVM> addresses, String id) {
        for (AddressVM v : addresses)
            v.setDefault(false);
        addresses.stream().filter(a -> Objects.equals(a.getId(), id)).findFirst()
                .ifPresent(a -> a.setDefault(true));
    }

    @GetMapping("/addresses")
    public String addressesPage(Model model, HttpSession session) {
        List<AddressVM> list = getAddresses(session);
        model.addAttribute("addresses", list);
        model.addAttribute("formMode", "create");
        model.addAttribute("addressForm", new AddressForm());
        return "user/address/manage";
    }

    @GetMapping("/addresses/{id}/edit")
    public String editAddress(@PathVariable String id, Model model, HttpSession session) {
        List<AddressVM> list = getAddresses(session);
        AddressVM vm = list.stream().filter(a -> Objects.equals(a.getId(), id)).findFirst()
                .orElse(null);
        if (vm == null) {
            model.addAttribute("error", "Không tìm thấy địa chỉ.");
            model.addAttribute("addresses", list);
            model.addAttribute("formMode", "create");
            model.addAttribute("addressForm", new AddressForm());
            return "user/address/manage";
        }
        AddressForm f = new AddressForm();
        f.setId(vm.getId());
        f.setFullName(vm.getFullName());
        f.setPhone(vm.getPhone());
        f.setLine(vm.getLine());
        f.setWard(vm.getWard());
        f.setDistrict(vm.getDistrict());
        f.setCity(vm.getCity());
        f.setAsDefault(vm.isDefault());

        model.addAttribute("addresses", list);
        model.addAttribute("formMode", "edit");
        model.addAttribute("addressForm", f);
        return "user/address/manage";
    }

    @PostMapping("/addresses/create")
    public String createAddress(@ModelAttribute AddressForm form, HttpSession session,
            org.springframework.web.servlet.mvc.support.RedirectAttributes ra) {
        List<AddressVM> list = getAddresses(session);
        AddressVM vm = AddressVM.fromForm(form);
        vm.setId(UUID.randomUUID().toString());
        if (Boolean.TRUE.equals(form.getAsDefault())) {
            setDefault(list, vm.getId()); // clear trước
            vm.setDefault(true);
        }
        list.add(vm);
        ra.addFlashAttribute("toastSuccess", "Đã thêm địa chỉ!");
        return "redirect:/user/addresses";
    }

    @PostMapping("/addresses/update")
    public String updateAddress(@ModelAttribute AddressForm form, HttpSession session,
            org.springframework.web.servlet.mvc.support.RedirectAttributes ra) {
        List<AddressVM> list = getAddresses(session);
        AddressVM vm = list.stream().filter(a -> Objects.equals(a.getId(), form.getId())).findFirst()
                .orElse(null);
        if (vm == null) {
            ra.addFlashAttribute("toastError", "Không tìm thấy địa chỉ để cập nhật.");
            return "redirect:/user/addresses";
        }
        vm.setFullName(form.getFullName());
        vm.setPhone(form.getPhone());
        vm.setLine(form.getLine());
        vm.setWard(form.getWard());
        vm.setDistrict(form.getDistrict());
        vm.setCity(form.getCity());

        if (Boolean.TRUE.equals(form.getAsDefault())) {
            setDefault(list, vm.getId());
        }
        ra.addFlashAttribute("toastSuccess", "Đã lưu thay đổi!");
        return "redirect:/user/addresses";
    }

    @PostMapping("/addresses/{id}/delete")
    public String deleteAddress(@PathVariable String id, HttpSession session,
            org.springframework.web.servlet.mvc.support.RedirectAttributes ra) {
        List<AddressVM> list = getAddresses(session);
        boolean removed = list.removeIf(a -> Objects.equals(a.getId(), id));
        if (removed) {
            ra.addFlashAttribute("toastSuccess", "Đã xoá địa chỉ!");
        } else {
            ra.addFlashAttribute("toastError", "Không tìm thấy địa chỉ để xoá.");
        }
        return "redirect:/user/addresses";
    }

    @PostMapping("/addresses/{id}/default")
    public String setDefaultAddress(@PathVariable String id, HttpSession session,
            org.springframework.web.servlet.mvc.support.RedirectAttributes ra) {
        List<AddressVM> list = getAddresses(session);
        if (list.stream().noneMatch(a -> Objects.equals(a.getId(), id))) {
            ra.addFlashAttribute("toastError", "Không tìm thấy địa chỉ.");
            return "redirect:/user/addresses";
        }
        setDefault(list, id);
        ra.addFlashAttribute("toastSuccess", "Đã đặt làm mặc định!");
        return "redirect:/user/addresses";
    }

    // ===== CART =====
    @PostMapping("/cart")
    public String addToCartFromForm(
            @RequestParam(value = "itemId", required = false) String itemId,
            @RequestParam(value = "productId", required = false) String productId, // form hiện tại dùng productId
            @RequestParam(value = "branchId", required = false) String branchId,
            @RequestParam(value = "quantity", required = false) Integer quantity,
            Authentication auth,
            org.springframework.web.servlet.mvc.support.RedirectAttributes ra) {

        String resolvedItemId = (itemId != null && !itemId.isBlank()) ? itemId : productId;

        if (resolvedItemId == null || resolvedItemId.isBlank()) {
            ra.addFlashAttribute("toastError", "Thiếu mã sản phẩm.");
            return "redirect:/user/cart";
        }

        String userId = (auth != null && auth.isAuthenticated()) ? auth.getName() : null;

        var req = new com.example.Alotrabong.dto.AddToCartRequest();
        req.setItemId(resolvedItemId);
        req.setBranchId(branchId);
        req.setQuantity(quantity);

        try {
            cartService.addToCart(userId, req);
            ra.addFlashAttribute("toastSuccess", "Đã thêm vào giỏ!");
        } catch (RuntimeException ex) {
            ra.addFlashAttribute("toastError", ex.getMessage());
        }

        String b = (branchId != null && !branchId.isBlank()) ? "?branchId=" + branchId : "";
        return "redirect:/user/cart" + b;
    }

    @GetMapping("/cart")
    public String cart(@RequestParam(required = false) String branchId,
            Authentication auth,
            Model model) {
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
            String thumb = itemMediaRepository.findByItem_ItemIdOrderBySortOrderAsc(d.getItemId())
                    .stream().findFirst().map(ItemMedia::getMediaUrl)
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

    // ===== CHECKOUT (GET ONLY – POST nằm ở CheckoutFlowController) =====
    @GetMapping("/checkout")
    public String checkout(@RequestParam(required = false, defaultValue = "b1") String branchId,
            Authentication auth,
            HttpSession session,
            Model model) {

        String userIdOrLogin = (auth != null && auth.isAuthenticated())
                ? auth.getName()
                : "user-id-placeholder";

        var dtos = cartService.getCartItems(userIdOrLogin, branchId);
        var items = new ArrayList<Map<String, Object>>();
        for (var d : dtos) {
            var it = new HashMap<String, Object>();
            String thumb = itemMediaRepository.findByItem_ItemIdOrderBySortOrderAsc(d.getItemId())
                    .stream().findFirst().map(ItemMedia::getMediaUrl)
                    .orElse("/img/products/" + d.getItemId() + ".jpg");
            it.put("itemId", d.getItemId());
            it.put("name", d.getItemName());
            it.put("thumbnailUrl", thumb);
            it.put("unitPrice", d.getUnitPrice());
            it.put("qty", d.getQuantity());
            it.put("subtotal", d.getTotalPrice());
            items.add(it);
        }

        BigDecimal subtotal = dtos.stream()
                .map(CartItemDTO::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        String appliedCode = (String) session.getAttribute("appliedCouponCode");
        BigDecimal discount = "ALO20".equalsIgnoreCase(appliedCode)
                ? new BigDecimal("20000")
                : BigDecimal.ZERO;

        BigDecimal shipping = new BigDecimal("15000");
        BigDecimal grand = subtotal.add(shipping).subtract(discount);

        var summary = new HashMap<String, BigDecimal>();
        summary.put("subtotal", subtotal);
        summary.put("discount", discount);
        summary.put("shippingFee", shipping);
        summary.put("grandTotal", grand);

        var addresses = new ArrayList<Map<String, Object>>();
        addresses.add(Map.of(
                "id", "addr-1",
                "fullName", "Nguyễn Văn A",
                "phone", "0900000000",
                "line", "Số 1 đường X",
                "ward", "P.Y",
                "district", "Q.Z",
                "city", "TP.HCM",
                "default", true));

        String selectedAddressId = (String) session.getAttribute("selectedAddressId");
        String selectedPayment = (String) session.getAttribute("selectedPayment");
        String note = (String) session.getAttribute("checkoutNote");
        String couponError = (String) session.getAttribute("couponError");

        model.addAttribute("items", items);
        model.addAttribute("summary", summary);
        model.addAttribute("branchId", branchId);
        model.addAttribute("addresses", addresses);
        model.addAttribute("selectedAddressId", selectedAddressId);
        model.addAttribute("selectedPayment", selectedPayment);
        model.addAttribute("note", note);
        model.addAttribute("appliedCoupon", appliedCode != null ? Map.of("code", appliedCode) : null);
        model.addAttribute("couponError", couponError);

        return "user/checkout/checkout";
    }

    // ===== ORDER / FAVORITE / RECENT / COUPON / REVIEW / CHAT =====
    @GetMapping("/order/history")
    public String orderHistory() {
        return "user/order/history";
    }

    @GetMapping("/order/detail/{code}")
    public String orderDetail(@PathVariable String code) {
        return "user/order/detail";
    }

    @GetMapping("/address/manage")
    public String addressManage() {
        return "user/address/manage";
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
            HttpSession session,
            org.springframework.web.servlet.mvc.support.RedirectAttributes ra) {
        var itemOpt = itemRepository.findById(itemId);
        if (itemOpt.isEmpty()) {
            ra.addFlashAttribute("toastError", "Món không tồn tại.");
            return "redirect:/user/favorite/list";
        }
        var fav = getFavoriteSet(session);
        fav.add(itemId);
        ra.addFlashAttribute("toastSuccess", "Đã thêm vào yêu thích!");
        return "redirect:/user/favorite/list";
    }

    @PostMapping("/favorite/{id}/remove")
    public String removeFavorite(@PathVariable("id") String itemId,
            HttpSession session,
            org.springframework.web.servlet.mvc.support.RedirectAttributes ra) {
        var fav = getFavoriteSet(session);
        if (fav.remove(itemId)) {
            ra.addFlashAttribute("toastSuccess", "Đã bỏ khỏi yêu thích!");
        } else {
            ra.addFlashAttribute("toastError", "Món không nằm trong yêu thích.");
        }
        return "redirect:/user/favorite/list";
    }

    @GetMapping("/favorite/list")
    public String favoriteList(Model model, HttpSession session) {
        var fav = getFavoriteSet(session);

        // rỗng thì trả sớm
        if (fav.isEmpty()) {
            model.addAttribute("items", java.util.Collections.emptyList());
            model.addAttribute("page", null);
            return "user/favorite/list";
        }

        // map dữ liệu tối thiểu cho view
        var items = itemRepository.findAllById(fav).stream().map(i -> {
            var thumb = itemMediaRepository.findByItem_ItemIdOrderBySortOrderAsc(i.getItemId())
                    .stream().findFirst().map(ItemMedia::getMediaUrl)
                    .orElse("/img/products/" + i.getItemId() + ".jpg");
            var m = new java.util.HashMap<String, Object>();
            m.put("id", i.getItemId());
            m.put("name", i.getName());
            m.put("price", i.getPrice());
            m.put("thumbnailUrl", thumb);
            m.put("slug", i.getItemCode());
            return m;
        }).toList();

        model.addAttribute("items", items);
        model.addAttribute("page", null); // nếu cần phân trang, bạn tự wrap PageImpl như ở product list
        return "user/favorite/list";
    }

    @GetMapping("/recent/list")
    public String recentList() {
        return "user/recent/list";
    }

    // ===== COUPON =====
    @GetMapping("/coupon/list")
    public String couponList(Model model, HttpSession session) {
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
            Model model) {

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
    public String chatRoom(@PathVariable String roomId) {
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

    // ===== legacy redirects: hứng /checkout cũ rồi redirect về /user/checkout
    // =====
    @Controller("legacyRedirectController")
    @PreAuthorize("permitAll()")
    static class UserLegacyRedirects {
        @GetMapping("/checkout")
        public String redirectCheckoutLegacy() {
            return "redirect:/user/checkout";
        }
    }
}
