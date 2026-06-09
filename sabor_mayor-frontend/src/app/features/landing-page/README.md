# Landing Page - Sabor Mayor

## Estructura de Componentes

La landing page está dividida en componentes independientes siguiendo los principios de arquitectura modular de Angular. Cada sección es un componente standalone que mantiene sus propios estilos, lógica y datos.

```
landing-page/
├── landing-page.ts          # Componente contenedor principal
├── landing-page.html        # Template que orquesta los componentes
├── landing-page.scss        # Estilos base del contenedor
├── _landing-config.scss     # Variables, mixins y configuración compartida
├── sections/                # Carpeta con todos los componentes de sección
│   ├── hero-section/        # Sección héroe con CTA
│   ├── about-section/       # Sección "Nuestra Historia"
│   ├── featured-dishes-section/  # Platos destacados
│   ├── wine-selection-section/   # Selección de vinos
│   ├── experience-section/  # Experiencias culinarias
│   ├── testimonials-section/ # Reseñas de clientes
│   ├── contact-reservation-section/ # Formulario de contacto
│   └── footer-section/      # Footer con enlaces
└── README.md                # Este archivo
```

## Componentes

### 1. **HeroSectionComponent** (`hero-section/`)
- **Propósito**: Presenta el brand con impacto visual
- **Características**: Título grande, subtítulo, dual CTA (Reservar/Explorar)
- **Diseño**: Imagen de hero en lado derecho, texto en izquierda
- **Responsive**: Apila verticalmente en mobile

### 2. **AboutSectionComponent** (`about-section/`)
- **Propósito**: Cuenta la historia de la marca
- **Características**: Grid 2 columnas (texto + imagen)
- **Contenido**: Narrativa sobre Sabor Mayor y el chef
- **Responsive**: Una columna en mobile

### 3. **FeaturedDishesSectionComponent** (`featured-dishes-section/`)
- **Propósito**: Muestra los platos estrella
- **Características**: Grid de 3 cards dinámicas
- **Datos**: Array de dishes con nombre, descripción, precio, tags
- **Interacción**: Hover effects, tags con colores según tipo

### 4. **WineSelectionSectionComponent** (`wine-selection-section/`)
- **Propósito**: Catálogo de vinos premium
- **Características**: Grid de 3 cards con región y descripción
- **Datos**: Array de wines con información de origen
- **Diseño**: Cards elegantes con hover

### 5. **ExperienceSectionComponent** (`experience-section/`)
- **Propósito**: Experiencias culinarias especiales
- **Características**: 3 cards apiladas verticalmente
- **Datos**: Título, duración, descripción, precio, CTA
- **Diseño**: Borde izquierdo de acento

### 6. **TestimonialsSectionComponent** (`testimonials-section/`)
- **Propósito**: Social proof con testimonios
- **Características**: Grid de 3 cards con reseñas
- **Datos**: Texto, autor, rol, rating dinámico
- **Diseño**: Estrellitas dinámicas, separador visual

### 7. **ContactReservationSectionComponent** (`contact-reservation-section/`)
- **Propósito**: Conversión: contacto + formulario de reserva
- **Características**: 2 columnas (info + form)
- **Validación**: ngModel para two-way binding
- **Diseño**: Inputs minimalistas con underline

### 8. **FooterSectionComponent** (`footer-section/`)
- **Propósito**: Links, legal, redes sociales
- **Características**: Grid de 4 secciones dinámicas
- **Datos**: Array de secciones con links
- **Diseño**: Fondo más oscuro, separador de cobre

## Design System

### Variables Centralizadas (`_landing-config.scss`)

#### Colores (De `_variables.scss`)
```scss
--tierra-profunda: #1c1208    // Fondo principal
--ambar-dorado: #c8902a      // Color primario
--crema-antigua: #f4ecd8     // Texto principal
--cobre-vivo: #a0522d        // Acentos
--verde-selva: #2d5016       // Tags positivos
--rojo-aji: #8b1a1a          // Tags de alerta
--humo-suave: #3a2e22        // Cards alternativas
--marfil-puro: #fafaf7       // Fondos claros (raramente usado)
```

#### Tipografía (De fonts de Google)
- **Display**: `EB Garamond` (títulos heroicos)
- **Headings**: `Playfair Display` (títulos de sección)
- **Body**: `DM Sans` (texto legible)
- **Monospace**: `Space Mono` (precios, etiquetas)

#### Espaciado
```scss
$spacing-unit: 8px            // Base
$spacing-2xl: 80px            // Márgenes de sección
$spacing-3xl: 120px           // Gaps entre secciones
$container-max: 1200px        // Ancho máximo
```

### Mixins Disponibles

#### Secciones
```scss
@include section-base;        // Padding, background, grano
@include container;           // Max-width, gutter responsive
```

#### Tipografía
```scss
@include title-lg;            // 3.5rem, EB Garamond, dorado
@include title-md;            // 2.5rem, Playfair, dorado
@include title-sm;            // 1.5rem, Playfair, crema
@include body-text-lg;        // 1.125rem, legible
@include body-text-muted;     // Texto secundario
@include body-text-accent;    // Monospace, etiquetas
```

#### Botones
```scss
@include btn-primary;         // Dorado sólido
@include btn-secondary;       // Borde dorado
@include btn-outline;         // Borde suave
```

#### Efectos
```scss
@include grain-texture;       // Grano analógico (firma del header)
@include divider-gold;        // Línea dorada
@include divider-copper;      // Línea de cobre
@include transition-smooth;   // ease cúbica, 0.3s
```

## Flujo de Datos

Cada componente maneja sus propios datos:

```typescript
// Featured Dishes
dishes: Dish[] = [{ name, description, price, image, tag, tagType }]

// Wines
wines: Wine[] = [{ name, region, description, price, image }]

// Experiences
experiences: Experience[] = [{ title, duration, description, price, buttonText }]

// Testimonials
testimonials: Testimonial[] = [{ text, author, role, rating }]

// Footer
footerSections: FooterSection[] = [{ title, content?, links? }]
```

## Patrones de Diseño

### 1. **Responsive First**
Cada componente define mobile como base, luego breakpoints:
- Mobile: < 480px
- Tablet: < 768px
- Desktop: 1024px+

### 2. **Hover Effects Sutiles**
- Cards: `translateY(-4px)` + opacity change
- Imágenes: `scale(1.05)` smooth
- Links: color transition a dorado

### 3. **Consistencia Visual**
- Todos los títulos: Garamond/Playfair + dorado
- Todos los subtítulos: muted gold
- Todos los precios: monospace + dorado principal
- Todos los borders: 1px subtle copper o gold

### 4. **Grano Signature**
El efecto de grano SVG del header se replica en cada sección usando `@include grain-texture` para mantener coherencia.

## Extensibilidad

Para agregar una nueva sección:

1. Crear carpeta en `sections/`
2. Generar `.ts`, `.html`, `.scss`
3. Importar `_landing-config.scss`
4. Usar mixins para consistencia
5. Importar en `landing-page.ts`
6. Agregar selector en `landing-page.html`

## Notas de Implementación

- ✅ **Standalone components**: Todos los componentes son standalone, sin módulos
- ✅ **No global SCSS**: Cada componente tiene scoped styles
- ✅ **Accessibility**: Headings semánticos, labels en forms
- ✅ **Performance**: Lazy loading potencial en imágenes
- ✅ **Mobile-first**: Responsive desde 320px
- ✅ **Paleta fiel**: 100% alineado con header.scss y _variables.scss

## Diseñador: 40 años de experiencia UI/UX

Esta arquitectura refleja principios de:
- **Modularidad**: Cada sección es independiente y reutilizable
- **Consistencia**: Sistema de design unificado via mixins
- **Mantenibilidad**: Cambios centralizados en `_landing-config.scss`
- **Escalabilidad**: Fácil agregar nuevas secciones
- **Performance**: Componentes ligeros, estilos optimizados
