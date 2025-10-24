const container = document.querySelector('.container');
const registerBtn = document.querySelector('.register-btn');
const loginBtn = document.querySelector('.login-btn');

registerBtn.addEventListener('click', () => {
  container.classList.add('active');
});

loginBtn.addEventListener('click', () => {
  container.classList.remove('active');
});
// ===== FORM ELEMENTS (không cần id) =====
const loginForm = document.querySelector(".form-box.login form");
const registerForm = document.querySelector(".form-box.register form");
const forgotLink = document.querySelector(".form-box.login .forgot-link a");

// ===== helper =====
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
  Object.entries(obj).map(([k,v]) => `${encodeURIComponent(k)}=${encodeURIComponent(v)}`).join("&");

const flash = (form, type, msg) => {
  if (!form) return;
  let box = form.querySelector(".msg");
  if (!box) {
    box = document.createElement("p");
    box.className = "msg";
    form.querySelector("h1")?.after(box);
  }
  box.textContent = msg || "";
  box.className = `msg ${type}`; // cần CSS: .msg, .msg.success, .msg.error
};

// ===== REGISTER =====
registerForm?.addEventListener("submit", async (e) => {
  e.preventDefault();
  const f = e.target;

  const payload = {
    email:    f.email?.value?.trim(),
    password: f.password?.value,
    fullName: f.fullName?.value?.trim(),       // <-- BẮT BUỘC
    phone: f.phone?.value?.trim() || null,     // optional
  };

  // client-side guard để khỏi spam request fail
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
    flash(f, "success", json.message || "Đăng ký thành công! Vui lòng kiểm tra email để lấy mã OTP.");
    // Hiển thị form nhập OTP
    showOtpForm(f.email?.value?.trim());
  } else {
    flash(f, "error", json.message || "Đăng ký thất bại");
  }
});


// ===== LOGIN =====
// LƯU Ý: DTO của bạn là email + password -> input name phải là "email"
// Form login sẽ submit thông thường, không cần JavaScript
// Spring Security sẽ xử lý authentication và redirect

// ===== OTP VERIFICATION =====
function showOtpForm(email) {
  const otpModal = document.createElement('div');
  otpModal.className = 'otp-modal';
  otpModal.innerHTML = `
    <div class="otp-modal-content">
      <h3>Xác thực OTP</h3>
      <p>Mã OTP đã được gửi đến email: <strong>${email}</strong></p>
      <form id="otpForm">
        <div class="input-box">
          <input type="text" name="otp" placeholder="Nhập mã OTP" maxlength="6" required>
          <i class='bx bx-key'></i>
        </div>
        <button type="submit" class="btn">Xác thực</button>
        <button type="button" class="btn btn-secondary" onclick="closeOtpModal()">Hủy</button>
      </form>
    </div>
  `;
  
  // Thêm CSS cho modal
  const style = document.createElement('style');
  style.textContent = `
    .otp-modal {
      position: fixed;
      top: 0;
      left: 0;
      width: 100%;
      height: 100%;
      background: rgba(0,0,0,0.5);
      display: flex;
      justify-content: center;
      align-items: center;
      z-index: 1000;
    }
    .otp-modal-content {
      background: white;
      padding: 30px;
      border-radius: 15px;
      box-shadow: 0 10px 30px rgba(0,0,0,0.3);
      max-width: 400px;
      width: 90%;
      text-align: center;
    }
    .otp-modal-content h3 {
      margin-bottom: 15px;
      color: #333;
    }
    .otp-modal-content p {
      margin-bottom: 20px;
      color: #666;
    }
  `;
  document.head.appendChild(style);
  document.body.appendChild(otpModal);
  
  // Xử lý form OTP
  document.getElementById('otpForm').addEventListener('submit', async (e) => {
    e.preventDefault();
    const otp = e.target.otp.value.trim();
    
    if (!otp || otp.length !== 6) {
      flash(e.target, "error", "Mã OTP phải có 6 chữ số!");
      return;
    }
    
    flash(e.target, "", "");
    const json = await api("/api/auth/verify-otp", { email, otp });
    
    if (json.success) {
      flash(e.target, "success", "Xác thực thành công! Bạn có thể đăng nhập ngay bây giờ.");
      setTimeout(() => {
        closeOtpModal();
        loginBtn?.click();
      }, 1500);
    } else {
      flash(e.target, "error", json.message || "Mã OTP không đúng hoặc đã hết hạn");
    }
  });
}

function closeOtpModal() {
  const modal = document.querySelector('.otp-modal');
  if (modal) {
    modal.remove();
  }
}

// ===== FORGOT PASSWORD (gửi OTP reset) =====
forgotLink?.addEventListener("click", async (e) => {
  e.preventDefault();
  const f = loginForm;
  const email = f?.email?.value?.trim();
  if (!email) {
    flash(f, "error", "Nhập email trước đã nha!");
    return;
  }

  flash(f, "", "");
  const json = await fetch(`/api/auth/forgot-password?${qs({ email })}`, {
    method: "POST",
  }).then(r => r.json()).catch(() => ({ success: false }));

  if (json?.success) {
    flash(f, "success", json.message || "Đã gửi OTP reset về email. Vào mail kiểm tra nha!");
    // Hiển thị form nhập OTP cho reset password
    showPasswordResetForm(email);
  } else {
    flash(f, "error", json?.message || "Gửi OTP thất bại");
  }
});

// ===== PASSWORD RESET FORM =====
function showPasswordResetForm(email) {
  const resetModal = document.createElement('div');
  resetModal.className = 'otp-modal';
  resetModal.innerHTML = `
    <div class="otp-modal-content">
      <h3>Đặt lại mật khẩu</h3>
      <p>Mã OTP đã được gửi đến email: <strong>${email}</strong></p>
      <form id="resetPasswordForm">
        <div class="input-box">
          <input type="text" name="otp" placeholder="Nhập mã OTP" maxlength="6" required>
          <i class='bx bx-key'></i>
        </div>
        <div class="input-box">
          <input type="password" name="newPassword" placeholder="Mật khẩu mới" required>
          <i class='bx bxs-lock-alt'></i>
        </div>
        <button type="submit" class="btn">Đặt lại mật khẩu</button>
        <button type="button" class="btn btn-secondary" onclick="closeOtpModal()">Hủy</button>
      </form>
    </div>
  `;
  
  document.body.appendChild(resetModal);
  
  // Xử lý form reset password
  document.getElementById('resetPasswordForm').addEventListener('submit', async (e) => {
    e.preventDefault();
    const otp = e.target.otp.value.trim();
    const newPassword = e.target.newPassword.value;
    
    if (!otp || otp.length !== 6) {
      flash(e.target, "error", "Mã OTP phải có 6 chữ số!");
      return;
    }
    
    if (newPassword.length < 6) {
      flash(e.target, "error", "Mật khẩu phải có ít nhất 6 ký tự!");
      return;
    }
    
    flash(e.target, "", "");
    const json = await api("/api/auth/reset-password", { email, otp, newPassword });
    
    if (json.success) {
      flash(e.target, "success", "Đặt lại mật khẩu thành công! Bạn có thể đăng nhập với mật khẩu mới.");
      setTimeout(() => {
        closeOtpModal();
        loginBtn?.click();
      }, 1500);
    } else {
      flash(e.target, "error", json.message || "Đặt lại mật khẩu thất bại");
    }
  });
}