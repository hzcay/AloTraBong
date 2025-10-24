// ====== TOGGLE LOGIN / REGISTER ======
const container   = document.querySelector(".container");
const registerBtn = document.querySelector(".register-btn");
const loginBtn    = document.querySelector(".login-btn");

registerBtn?.addEventListener("click", () => container?.classList.add("active"));
loginBtn?.addEventListener("click", () => container?.classList.remove("active"));

// ====== FORM ELEMENTS ======
const loginForm    = document.querySelector(".form-box.login form");
const registerForm = document.querySelector(".form-box.register form");
const forgotLink   = document.querySelector(".form-box.login .forgot-link a");

// ====== HELPERS ======
const api = async (url, body, method = "POST") => {
  const res = await fetch(url, {
    method,
    headers: { "Content-Type": "application/json" },
    body: body ? JSON.stringify(body) : undefined,
  });
  let json;
  try { json = await res.json(); } catch { json = { success: false, message: "Invalid response" }; }
  return json;
};

const qs = (obj) =>
  Object.entries(obj).map(([k, v]) => `${encodeURIComponent(k)}=${encodeURIComponent(v)}`).join("&");

const flash = (form, type, msg) => {
  if (!form) return;
  let box = form.querySelector(".msg");
  if (!box) {
    box = document.createElement("p");
    box.className = "msg";
    form.querySelector("h1,h3")?.after(box);
  }
  box.textContent = msg || "";
  box.className = `msg ${type || ""}`; // cần CSS: .msg, .msg.success, .msg.error
};

// ====== CENTERED NOTIFICATION CARD (no dark overlay) ======
const MODAL_SEL = ".otp-modal";

function ensureSingleModal() {
  document.querySelectorAll(MODAL_SEL).forEach((m) => m.remove());
}

function openModal(innerHTML, title = "Xác thực", subtitle = "") {
  ensureSingleModal();

  const modal = document.createElement("div");
  modal.className = "otp-modal";
  modal.style.cssText = `
    position: fixed; inset: 0; display: flex; align-items: center; justify-content: center;
    background: transparent; z-index: 1000; pointer-events: none; /* không chặn click nền */
  `;

  // card nhỏ ở giữa
  modal.innerHTML = `
    <div class="otp-modal-content"
         style="
          max-width:480px; width: min(92vw, 480px);
          background: #fff; border-radius: 16px; box-shadow: 0 18px 50px rgba(0,0,0,.25);
          padding: 18px 20px; pointer-events: auto; /* chỉ card nhận click */
         ">
      <!-- Header -->
      <div style="display:flex; align-items:center; justify-content:space-between; gap:12px; margin-bottom:10px;">
        <div style="display:flex; align-items:center; gap:10px;">
          <div style="width:36px; height:36px; border-radius:10px; background:#ffb347; display:flex; align-items:center; justify-content:center; color:#fff; font-weight:700;">!</div>
          <div>
            <h3 style="margin:0; color:#333;">${title}</h3>
            ${subtitle ? `<p style="margin:2px 0 0; color:#666; font-size:14px;">${subtitle}</p>` : ""}
          </div>
        </div>
        <button data-close aria-label="Đóng"
          style="background:transparent;border:none;font-size:22px;line-height:1;cursor:pointer;color:#666;">×</button>
      </div>

      ${innerHTML}
    </div>
  `;

  document.body.appendChild(modal);

  // ESC để đóng
  const onEsc = (e) => { if (e.key === "Escape") closeModal(); };
  document.addEventListener("keydown", onEsc, { once: true });

  // nút ×
  modal.querySelector("[data-close]")?.addEventListener("click", closeModal);

  // auto-focus control đầu tiên
  setTimeout(() => modal.querySelector("input,button,select,textarea")?.focus(), 10);

  return modal;
}

function closeModal() {
  document.querySelector(MODAL_SEL)?.remove();
}


function closeModal() {
  document.querySelector(MODAL_SEL)?.remove();
}

// ====== REGISTER FLOW ======
registerForm?.addEventListener("submit", async (e) => {
  e.preventDefault();
  const f = e.target;

  const payload = {
    email:    f.email?.value?.trim(),
    password: f.password?.value,
    fullName: f.fullName?.value?.trim(),
    phone:    f.phone?.value?.trim() || null,
  };

  if (!payload.fullName) {
    flash(f, "error", "Điền Full name giùm cái nè!");
    return;
  }
  if (!payload.password || payload.password.length < 6) {
    flash(f, "error", "Password phải ≥ 6 ký tự nha!");
    return;
  }

  flash(f, "", "");
  const json = await api("/api/auth/register", payload);
  if (json.success) {
    flash(f, "success", json.message || "Đăng ký thành công! Check mail lấy OTP nha.");
    // mở modal xác thực OTP (UI đồng bộ)
    renderOtpVerifyForRegister(payload.email);
  } else {
    flash(f, "error", json.message || "Đăng ký thất bại");
  }
});

// ====== OTP VERIFY (xài cho đăng ký) ======
function renderOtpVerifyForRegister(email) {
  const html = `
    <form id="otpForm">
      <div class="input-box">
        <input type="text" name="otp" placeholder="Nhập mã OTP (6 số)" maxlength="6" required>
        <i class='bx bx-key'></i>
      </div>
      <p class="msg"></p>
      <div style="display:flex; gap:10px;">
        <button type="button" class="btn" id="cancelOtp" style="flex:1; opacity:.9;">Hủy</button>
        <button type="submit" class="btn" style="flex:1;">Xác thực</button>
      </div>
    </form>
  `;
  const modal = openModal(
    html,
    "Xác thực OTP",
    `Mã OTP đã được gửi đến: <strong style="color:#333;">${email}</strong>`
  );

  const form = modal.querySelector("#otpForm");
  modal.querySelector("#cancelOtp")?.addEventListener("click", closeModal);

  form.addEventListener("submit", async (e) => {
    e.preventDefault();
    const otp = form.otp.value.trim();
    if (!otp || otp.length !== 6) return flash(form, "error", "Mã OTP phải có 6 chữ số!");

    flash(form, "", "");
    const json = await api("/api/auth/verify-otp", { email, otp });
    if (json.success) {
      flash(form, "success", "Xác thực thành công! Bạn có thể đăng nhập ngay.");
      setTimeout(() => { closeModal(); loginBtn?.click(); }, 1200);
    } else {
      flash(form, "error", json.message || "Mã OTP không đúng hoặc đã hết hạn");
    }
  });
}

// ====== LOGIN FLOW ======
// Login form submit để Spring Security xử lý (không JS). Chỉ bắt "Quên mật khẩu".

// ====== FORGOT PASSWORD — 1 MODAL / 2 STEPS ======
forgotLink?.addEventListener("click", (e) => {
  e.preventDefault();
  const prefill = loginForm?.email?.value?.trim() || "";
  renderForgotStepEmail(prefill);
});

/** STEP 1 — Nhập email để nhận OTP */
function renderForgotStepEmail(prefillEmail = "") {
  const stepDots = `
    <div style="display:flex; gap:6px; align-items:center; margin:10px 0 16px;">
      <span style="width:8px;height:8px;border-radius:999px;background:#ffb347;"></span>
      <span style="width:8px;height:8px;border-radius:999px;background:#ddd;"></span>
    </div>`;

  const html = `
    ${stepDots}
    <form id="sendOtpForm" style="margin-top:6px;">
      <div class="input-box">
        <input type="email" name="email" placeholder="Email của bạn" required value="${prefillEmail}">
        <i class='bx bx-envelope'></i>
      </div>
      <p class="msg"></p>
      <button type="submit" class="btn">Gửi OTP</button>
    </form>
  `;

  const modal = openModal(
    html,
    "Quên mật khẩu",
    "Nhập email để nhận mã OTP đặt lại mật khẩu"
  );

  const form = modal.querySelector("#sendOtpForm");
  form.addEventListener("submit", async (e) => {
    e.preventDefault();
    const email = form.email.value.trim();
    if (!email) return flash(form, "error", "Nhập email trước đã nha!");

    flash(form, "", "");
    try {
      const res = await fetch(`/api/auth/forgot-password?${qs({ email })}`, { method: "POST" });
      const json = await res.json();
      if (json?.success) {
        renderForgotStepReset(email);
      } else {
        flash(form, "error", json?.message || "Gửi OTP thất bại");
      }
    } catch {
      flash(form, "error", "Có lỗi mạng, thử lại sau nha!");
    }
  });
}

/** STEP 2 — Nhập OTP + mật khẩu mới */
function renderForgotStepReset(email) {
  const stepDots = `
    <div style="display:flex; gap:6px; align-items:center; margin:10px 0 16px;">
      <span style="width:8px;height:8px;border-radius:999px;background:#ddd;"></span>
      <span style="width:8px;height:8px;border-radius:999px;background:#ffb347;"></span>
    </div>`;

  const html = `
    ${stepDots}
    <div style="margin:-4px 0 10px; color:#666; font-size:14px;">
      Mã OTP đã gửi tới: <strong style="color:#333;">${email}</strong>
    </div>
    <form id="resetPasswordForm">
      <div class="input-box">
        <input type="text" name="otp" placeholder="Nhập mã OTP (6 số)" maxlength="6" required>
        <i class='bx bx-key'></i>
      </div>
      <div class="input-box">
        <input type="password" name="newPassword" placeholder="Mật khẩu mới (≥ 6 ký tự)" required>
        <i class='bx bxs-lock-alt'></i>
      </div>
      <div class="input-box">
        <input type="password" name="confirmPassword" placeholder="Nhập lại mật khẩu mới" required>
        <i class='bx bxs-lock'></i>
      </div>
      <p class="msg"></p>
      <div style="display:flex; gap:10px;">
        <button type="button" class="btn" id="backToEmail" style="flex:1; opacity:.9;">Quay lại</button>
        <button type="submit" class="btn" style="flex:1;">Xác nhận</button>
      </div>
    </form>
  `;

  const modal = openModal(
    html,
    "Đặt lại mật khẩu",
    "Nhập OTP và tạo mật khẩu mới"
  );

  const form = modal.querySelector("#resetPasswordForm");
  modal.querySelector("#backToEmail")?.addEventListener("click", () => renderForgotStepEmail(email));

  form.addEventListener("submit", async (e) => {
    e.preventDefault();
    const otp  = form.otp.value.trim();
    const pass = form.newPassword.value;
    const pass2 = form.confirmPassword.value;

    if (!otp || otp.length !== 6) return flash(form, "error", "Mã OTP phải có 6 chữ số!");
    if (!pass || pass.length < 6) return flash(form, "error", "Mật khẩu phải có ít nhất 6 ký tự!");
    if (pass !== pass2) return flash(form, "error", "Mật khẩu nhập lại không khớp!");

    flash(form, "", "");
    const json = await api("/api/auth/reset-password", { email, otp, newPassword: pass });
    if (json.success) {
      flash(form, "success", "Đặt lại mật khẩu thành công! Bạn có thể đăng nhập ngay.");
      setTimeout(() => { closeModal(); loginBtn?.click(); }, 1200);
    } else {
      flash(form, "error", json.message || "Đặt lại mật khẩu thất bại");
    }
  });
}
