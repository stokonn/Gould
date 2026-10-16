# Bundled typography

Downloaded from the Google Fonts upstream repository on 2026-10-16:

- `https://github.com/google/fonts/tree/main/ofl/inter`: `Inter[opsz,wght].ttf`
- `https://github.com/google/fonts/tree/main/ofl/cormorantgaramond`: `CormorantGaramond[wght].ttf`

Both are licensed under the SIL Open Font License 1.1. The unmodified font binaries are bundled under `app/src/main/res/font`; the complete upstream license files are included both here and inside the APK under `assets/licenses`. No downloadable-font provider or network font loading is used.

English and Czech diacritic coverage was checked against the bundled fonts using Fontconfig `fc-query`. Interface text, controls, statistics and timer digits use Inter; editorial headings and composition titles use Cormorant Garamond. Selecting System Font uses `FontFamily.Default` for every typography role.
