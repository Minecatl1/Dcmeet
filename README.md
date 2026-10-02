# Dcmeet Desktop

Dcmeet is an Android shell for a **contained Linux desktop**. It is designed to
launch a signed Linux root filesystem with either FEX-Emu or Box64, provide an
apt/Flatpak-capable guest, and safely select local `.deb`, `.tar`, `.tar.gz`,
`.tgz`, and `.tar.xz` packages.

## Shared files

The **Create shared folder** control creates Dcmeet's app-specific external
storage folder: `Android/data/dev.dcmeet.desktop/files/Dcmeet/Shared`. The
runtime should bind this location into the guest as `/mnt/shared`, so Android
files placed there can be used by the Linux desktop. App-specific storage avoids
requesting broad media or all-files access; Android file managers may require an
explicit Android/data access grant to show it.

## Important architecture boundary

Android applications cannot transparently translate the system calls of every
other Android application from ARM to x86_64. Dcmeet instead runs **Linux guest
programs** in its own app-private root filesystem through a user-space emulator.
It never runs a user-selected executable directly. A shipping build must provide
an audited, signed runtime bundle containing the selected engine, rootfs, display
server, and package installation helper.

## Build

Install Android SDK platform 35, set `ANDROID_HOME`, then run:

```sh
gradle :app:assembleDebug
```

The UI intentionally reports when the signed runtime bundle has not been
installed, rather than implying that package import or desktop launch is ready.

## Continuous integration

GitHub Actions builds a debug APK on pull requests, pushes to `main`, and manual
dispatches. The APK is uploaded as the `dcmeet-debug-apk` workflow artifact.
