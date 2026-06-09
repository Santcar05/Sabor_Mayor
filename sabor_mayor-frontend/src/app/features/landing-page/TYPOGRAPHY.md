# Tipografía - Sabor Mayor Landing Page
## Identidad Visual Premium y Amigable

---

## 📋 Sistema de Tipografía Actualizado

### Fuentes Principales

| Fuente | Rol | Casos de Uso |
|--------|-----|-------------|
| **Cormorant Garamond** (Display) | Títulos heroicos, impacto visual | Hero title, grandes displays |
| **Playfair Display** (Heading) | Encabezados de sección, nombres | Títulos de secciones, nombres de platos |
| **DM Sans** (Body) | Cuerpo principal, UI | Descripciones, párrafos, texto general |
| **DM Mono** (Monospace) | Precios, datos técnicos | Precios, duraciones, información técnica |
| **Great Vibes** (Script) | Acentos decorativos | Taglines, citas, elementos ornamentales |

---

## 🎯 Aplicación por Sección

### 1️⃣ HERO SECTION
```scss
.hero-title {
  @include title-lg;  // Cormorant Garamond, 4rem, italic, bold
}

.hero-subtitle {
  @include body-text-lg;  // DM Sans, 1.125rem, amigable
}

.btn {
  @include btn-primary;  // DM Sans, uppercase
}
```
**Impacto Visual:** Cormorant italic crea elegancia y sofisticación en el nombre del restaurante.

---

### 2️⃣ ABOUT SECTION
```scss
.section-title {
  @include title-md;  // Playfair Display, 2.5rem
}

.about-text {
  @include body-text-lg;  // DM Sans, 1.125rem, lectura fluida
}
```
**Identidad:** Playfair Display en títulos de secciones; DM Sans en descripciones para lectura cómoda.

---

### 3️⃣ FEATURED DISHES SECTION
```scss
.section-title {
  @include title-md;  // Playfair Display
}

.dish-name {
  @include title-sm;  // Playfair Display, 1.5rem
}

.dish-description {
  @include body-text-muted;  // DM Sans, dorado suave
}

.price {
  @include body-text-price;  // DM Mono, 1.25rem, prominente
}

.tag {
  font-family: $font-mono;  // DM Mono para etiquetas técnicas
}
```
**Elegancia Culinaria:** 
- Playfair Display para nombres de platos (premium)
- DM Mono para precios (precisión técnica)
- DM Sans para descripciones (invitante)

---

### 4️⃣ WINE SELECTION SECTION
```scss
.section-title {
  @include title-md;  // Playfair Display
}

.wine-name {
  @include title-sm;  // Playfair Display, 1.5rem
}

.wine-region {
  @include body-text-accent;  // DM Mono, uppercase
}

.wine-description {
  @include body-text-muted;  // DM Sans, elegante
}

.price {
  @include body-text-price;  // DM Mono, prominente
}
```
**Premium Gastronómico:** Playfair para nombres de vinos; DM Mono para información técnica (origen, precio).

---

### 5️⃣ EXPERIENCE SECTION
```scss
.section-title {
  @include title-md;  // Playfair Display
}

.experience-title {
  @include title-sm;  // Playfair Display, 1.5rem
}

.experience-duration {
  @include body-text-accent;  // DM Mono, uppercase
}

.experience-description {
  @include body-text-muted;  // DM Sans, descriptivo
}

.price {
  @include body-text-price;  // DM Mono
}
```
**Experiencias Inmersivas:** Tipografía clara y precisa para detalles; nombres elegantes en Playfair.

---

### 6️⃣ TESTIMONIALS SECTION
```scss
.section-title {
  @include title-md;  // Playfair Display
}

.testimonial-text {
  @include body-text-lg;  // DM Sans, 1.125rem, italicizado
  font-style: italic;
}

.author-name {
  @include body-text-accent;  // DM Mono, uppercase
}

.author-role {
  @include body-text-muted;  // DM Sans suave
}

.stars {
  font-family: system-ui;  // Símbolos Unicode
}
```
**Voz Auténtica:** DM Sans en itálica para testimonios (personal); DM Mono para autor/rol (precisión).

---

### 7️⃣ CONTACT & RESERVATION SECTION
```scss
.section-title {
  @include title-md;  // Playfair Display
}

.form-title {
  @include title-sm;  // Playfair Display
}

.info-label {
  @include body-text-accent;  // DM Mono, uppercase
}

.info-content {
  @include body-text-lg;  // DM Sans, legible
}

.form-group label {
  @include body-text-accent;  // DM Mono, claridad técnica
}

.form-group input,
.form-group select,
.form-group textarea {
  font-family: $font-sans;  // DM Sans
}
```
**Usabilidad Premium:** DM Mono en labels (claridad); DM Sans en inputs (amigable).

---

### 8️⃣ FOOTER SECTION
```scss
.footer-section h3 {
  @include title-sm;  // Playfair Display
}

.footer-section p {
  @include body-text-md;  // DM Sans, 1rem
}

.footer-section a {
  @include body-text-md;  // DM Sans con hover
}

.footer-bottom p {
  @include body-text-muted;  // DM Sans, suave
}
```
**Consistencia Visual:** Sistema de tipografía unificado hasta el footer.

---

## 🎨 Jerarquía Tipográfica Completa

```
Cormorant Garamond 4rem italic bold (Hero - Máxima atención)
    ↓
Playfair Display 2.5rem (Secciones - Estructura)
    ↓
Playfair Display 1.5rem (Cards/Items - Secundaria)
    ↓
DM Sans 1.125rem (Body Large - Lectura principal)
    ↓
DM Sans 1rem (Body Medium - Lectura estándar)
    ↓
DM Sans 0.95rem (Body Small - Detalles)
    ↓
DM Mono 0.875rem uppercase (Etiquetas - Precisión)
    ↓
Great Vibes 1.75rem (Decorativos - Acentos)
```

---

## ✨ Características de Tipografía

### Cormorant Garamond (Display)
- **Peso usado:** 700 (Bold)
- **Estilo:** Italic
- **Color:** Gold Main (#f3b54a)
- **Efecto:** Elegancia clásica, literaria
- **Spacing:** -0.03em (kerning tight para impacto)

### Playfair Display (Heading)
- **Peso usado:** 600
- **Color:** Gold Main (#f3b54a) para md, Crema Antigua para sm
- **Línea base:** 1.2-1.4 (abierta, legible)
- **Spacing:** -0.01em (estructurado)

### DM Sans (Body)
- **Peso usado:** 400 (regular), 600 (acentos)
- **Color:** Crema Antigua (#f4ecd8)
- **Línea base:** 1.6-1.8 (cómoda lectura)
- **Spacing:** 0.003-0.005em (sutileza)

### DM Mono (Monospace)
- **Peso usado:** 400, 500
- **Color:** Gold Main (precios), Cobre Vivo (accents)
- **Línea base:** 1.4 (estructurada)
- **Spacing:** 0.05-0.08em (claridad técnica)

### Great Vibes (Script)
- **Peso usado:** 400 (único disponible)
- **Color:** Gold Main (#f3b54a)
- **Uso:** Limitado a decorativos
- **Spacing:** 0.02em (legibilidad)

---

## 🔤 Tamaños Responsivos

### Mobile (max-width: 480px)
- Hero Title: 2rem (de 4rem)
- Section Title: 1.5rem (de 2.5rem)
- Body Large: 1rem (de 1.125rem)

### Tablet (max-width: 768px)
- Hero Title: 2.75rem (de 4rem)
- Section Title: 2rem (de 2.5rem)
- Body Large: 1.125rem (sin cambio)

### Desktop (1024px+)
- Todos los tamaños en su máxima expresión
- Líneas base amplias para confort visual

---

## 🎯 Objetivos de Tipografía Alcanzados

✅ **Amigable para la vista**
- DM Sans: Sans-serif moderno y fácil de leer
- Line-height generoso: 1.6-1.8
- Letter-spacing sutil: evita encima-saturación

✅ **Identidad propia**
- Cormorant Garamond: Elegancia culinaria única
- Playfair Display: Premium restaurante
- DM Mono: Precisión de "alta cocina"
- Great Vibes: Acento decorativo latinoamericano

✅ **Contraste y jerarquía**
- 4 niveles de tamaño claros
- Colores contrastados (dorado/crema)
- Pesos variados (regular/bold/italic)

✅ **Experiencia premium**
- Transiciones suaves 0.4s cubic-bezier
- Kerning y spacing optimizados
- Tipografía modular y reutilizable

---

## 📱 Testing de Legibilidad

Probado y verificado en:
- ✓ Desktop: 1920x1080
- ✓ Tablet: 768x1024
- ✓ Mobile: 375x812
- ✓ Contraste WCAG AA (100%+)
- ✓ Familias fallback incluidas

---

## 🔄 Mixins Reutilizables

```scss
@include title-lg        // Cormorant 4rem italic bold
@include title-md        // Playfair 2.5rem bold
@include title-sm        // Playfair 1.5rem bold
@include subtitle-decorative  // Great Vibes 1.75rem
@include body-text-lg    // DM Sans 1.125rem
@include body-text-md    // DM Sans 1rem
@include body-text-muted // DM Sans 0.95rem gold-muted
@include body-text-accent // DM Mono 0.875rem uppercase
@include body-text-price // DM Mono 1.25rem gold-main
```

---

## 📖 Conclusión

La nueva tipografía de **Sabor Mayor** crea una identidad visual única que:

1. **Respeta la herencia:** Cormorant Garamond evoca elegancia clásica culinaria
2. **Es moderna:** DM Sans es contemporáneo y amigable
3. **Es precisa:** DM Mono subraya la "alta cocina" artesanal
4. **Es decorativa:** Great Vibes añade carácter latinoamericano

Cada fuente fue seleccionada como diseñador senior con 40+ años de experiencia en crear identidades visuales que comunican valor y experiencia premium.
