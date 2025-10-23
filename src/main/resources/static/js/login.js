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
    username: f.username?.value?.trim(),
    email:    f.email?.value?.trim(),
    password: f.password?.value,
    fullName: f.fullName?.value?.trim(),       // <-- BẮT BUỘC
    // phone: f.phone?.value?.trim() || null,  // optional (nếu có input name="phone")
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
    flash(f, "success", json.message || "Đăng ký ok, check email nhận OTP nha!");
    setTimeout(() => loginBtn?.click(), 800);
  } else {
    flash(f, "error", json.message || "Đăng ký thất bại");
  }
});


// ===== LOGIN =====
// LƯU Ý: DTO của bạn là email + password -> input name phải là "email"
loginForm?.addEventListener("submit", async (e) => {
  e.preventDefault();
  const f = e.target;

  // nếu HTML còn name="username", đổi sang email trong file .html nha
  const payload = {
    email: f.email?.value?.trim(),       // <-- cần input name="email"
    password: f.password?.value,
  };

  flash(f, "", "");
  const json = await api("/api/auth/login", payload);
  if (json.success) {
    const { token } = json.data || {};
    if (token) localStorage.setItem("AT", token);
    flash(f, "success", "Đăng nhập thành công!");
    // điều hướng trang chủ (hoặc dashboard)
    setTimeout(() => (window.location.href = "/"), 500);
  } else {
    flash(f, "error", json.message || "Sai thông tin đăng nhập");
  }
});

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
    // bạn có thể mở modal nhập OTP + mật khẩu mới ở đây (nếu có UI)
    // hoặc dẫn qua trang /reset tùy bạn thiết kế
  } else {
    flash(f, "error", json?.message || "Gửi OTP thất bại");
  }
});