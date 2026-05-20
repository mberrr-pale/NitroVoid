# 🏎️ NitroVoid

> *Survive the streets. Master the nitro. Beat your best.*

NitroVoid adalah game balap atas-bawah (top-down racing) berbasis Java Swing sebagai project akhir. Pemain mengemudikan kendaraan di jalanan kota, menghindari musuh, mengumpulkan item, dan bertahan selama mungkin sambil memacu kecepatan setinggi-tingginya.

---

## 🎮 Cara Bermain

| Tombol | Aksi |
|---|---|
| `↑` / `W` | Gas — percepat kendaraan |
| `←` / `A` | Gerak kiri |
| `→` / `D` | Gerak kanan |
| `SHIFT` | Aktifkan Nitro |
| `C` | Aktifkan Slow Motion |
| `ESC` | Pause / Resume |
| `R` | Restart (saat Pause / Game Over) |
| `B` | Kembali ke Menu |
| `SPACE` | Lanjut (saat Story) |
| `ENTER` | Konfirmasi pilihan |

---

## ⚡ Fitur

- **Nitro System** — stok nitro terbatas dengan timing PERFECT / GOOD / MISS yang menentukan seberapa besar boost
- **Slow Motion** — perlambat semua musuh sementara untuk manuver darurat
- **Item System** — ambil item di jalan: Boost kecepatan, tambah Nitro, atau isi ulang Slow Motion
- **Difficulty Scaling** — setiap 500 skor, musuh makin cepat dan makin sering muncul
- **3 Map** — Ketintang, Liwet, Magetan, masing-masing dengan nuansa berbeda
- **Best Score** — skor terbaik per map tersimpan otomatis secara lokal
- **Feedback Visual** — teks PERFECT / BOOST / +5 SEC muncul langsung di layar saat aksi terjadi

---

## 🗂️ Struktur Project

```
com.nitrovoid
├── main/           → Entry point
├── game/           → Game loop, controller, state
├── entity/         → Player, Enemy, Item, Kendaraan
├── system/         → Nitro, SlowMotion, Timer, Score, Spawner, Save
├── input/          → Keyboard handler
├── ui/             → Semua screen & HUD
└── util/           → Collision detector, feedback text
```

---

## 🛠️ Teknologi

- **Java** — JDK 17+
- **Java Swing** — rendering & window
- **Java AWT** — grafis & input
- **`.properties` file** — penyimpanan best score lokal

---

## 🚀 Menjalankan Game

### Dari file `.jar`
```bash
java -jar NitroVoid.jar
```

### Dari file `.exe`
Langsung double-click `NitroVoid.exe`

### Dari source code
```bash
# Clone repo
git clone https://github.com/username/nitrovoid.git

# Masuk folder
cd nitrovoid

# Compile
javac -d bin src/com/nitrovoid/**/*.java

# Jalankan
java -cp bin com.nitrovoid.main.Main
```

---

## 💾 Save File

Best score tersimpan otomatis di file `nitrovoid_save.properties` yang dibuat di folder yang sama dengan game. File ini menyimpan info perangkat dan best score per map.

---

## 👥 Tim Pengembang

| Nama | Peran |
|---|---|
| Berlian | Logic Core |
| Ardian | Gameplay |
| Yuan | Gameplay |
| Syifa | UI |
| Suci | UI |
| Hamba Allah 

---

## 📄 Lisensi

Project ini dibuat untuk keperluan tugas akhir. Tidak untuk distribusi komersial.
