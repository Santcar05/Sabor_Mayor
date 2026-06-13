-- Carta Sabor Mayor (alineada con el frontend). Precios en COP.
INSERT INTO categories (id, name, slug, description, display_order) VALUES
    ('10000000-0000-0000-0000-000000000001', 'Entradas', 'entradas', 'Aperturas de sabor latinoamericano', 1),
    ('10000000-0000-0000-0000-000000000002', 'Platos Fuertes', 'fuertes', 'El corazon de nuestra cocina', 2),
    ('10000000-0000-0000-0000-000000000003', 'Postres', 'postres', 'Finales dulces de autor', 3),
    ('10000000-0000-0000-0000-000000000004', 'Cocteles', 'cocteles', 'Mixologia latinoamericana', 4),
    ('10000000-0000-0000-0000-000000000005', 'Vinos', 'vinos', 'Seleccion de vinos sudamericanos', 5);

INSERT INTO dishes (id, category_id, name, slug, description, price, cost, available, featured, image_url, prep_minutes) VALUES
    ('20000000-0000-0000-0000-000000000001', '10000000-0000-0000-0000-000000000001',
     'Ceviche de Maracuya Molecular', 'ceviche-de-maracuya-molecular',
     'Pesca del dia curada en leche de tigre de maracuya con esferificaciones citricas', 42000, 14700, TRUE, TRUE, '/assets/ceviche.jpg', 15),
    ('20000000-0000-0000-0000-000000000002', '10000000-0000-0000-0000-000000000001',
     'Texturas de Maiz Criollo', 'texturas-de-maiz-criollo',
     'Maiz andino en cinco texturas: espuma, crocante, cremoso, tostado y encurtido', 36000, 10800, TRUE, FALSE, NULL, 18),
    ('20000000-0000-0000-0000-000000000003', '10000000-0000-0000-0000-000000000001',
     'Ceviche Ancestral', 'ceviche-ancestral',
     'Receta tradicional peruana con aji limo, camote glaseado y cancha serrana', 38000, 13300, TRUE, TRUE, '/assets/ceviche.jpg', 12),
    ('20000000-0000-0000-0000-000000000004', '10000000-0000-0000-0000-000000000001',
     'Causa Limena Premium', 'causa-limena-premium',
     'Papa amarilla prensada, cangrejo real, palta y aji amarillo emulsionado', 34000, 11900, TRUE, TRUE, '/assets/Limenia.jpg', 14),
    ('20000000-0000-0000-0000-000000000005', '10000000-0000-0000-0000-000000000002',
     'Vacio en Madera de Olivo', 'vacio-en-madera-de-olivo',
     'Corte argentino madurado 28 dias, ahumado en madera de olivo con pure trufado', 89000, 35600, TRUE, TRUE, NULL, 35),
    ('20000000-0000-0000-0000-000000000006', '10000000-0000-0000-0000-000000000002',
     'Risotto de Quinoa y Setas', 'risotto-de-quinoa-y-setas',
     'Quinoa tricolor cremosa con setas silvestres y aceite de huacatay', 64000, 22400, TRUE, FALSE, NULL, 28),
    ('20000000-0000-0000-0000-000000000007', '10000000-0000-0000-0000-000000000002',
     'Aji de Gallina Contemporaneo', 'aji-de-gallina-contemporaneo',
     'Gallina de campo en crema de aji amarillo con esferas de papa nativa', 58000, 20300, TRUE, TRUE, '/assets/Aji_de_Gallina_Contemporaneo.jpg', 30),
    ('20000000-0000-0000-0000-000000000008', '10000000-0000-0000-0000-000000000003',
     'Volcan de Cacao Amazonico', 'volcan-de-cacao-amazonico',
     'Coulant de cacao 72% del Amazonas con helado de lucuma', 28000, 8400, TRUE, TRUE, NULL, 20),
    ('20000000-0000-0000-0000-000000000009', '10000000-0000-0000-0000-000000000003',
     'Suspiro Limeno Deconstruido', 'suspiro-limeno-deconstruido',
     'Manjar blanco aireado, merengue de oporto y crocante de almendra', 26000, 7800, TRUE, FALSE, NULL, 15),
    ('20000000-0000-0000-0000-000000000010', '10000000-0000-0000-0000-000000000004',
     'Clerico Mayor Elite', 'clerico-mayor-elite',
     'Vino blanco, frutas de temporada maceradas y toque de hierbabuena', 34000, 10200, TRUE, TRUE, NULL, 8),
    ('20000000-0000-0000-0000-000000000011', '10000000-0000-0000-0000-000000000004',
     'Pisco Sour Clasico', 'pisco-sour-clasico',
     'Pisco quebranta, limon sutil, jarabe de goma y amargo de angostura', 30000, 9000, TRUE, FALSE, NULL, 6),
    ('20000000-0000-0000-0000-000000000012', '10000000-0000-0000-0000-000000000005',
     'Malbec Reserva', 'malbec-reserva',
     'Mendoza, Argentina. Notas de ciruela madura, vainilla y final prolongado', 52000, 26000, TRUE, TRUE, '/assets/Malbec.jpg', NULL),
    ('20000000-0000-0000-0000-000000000013', '10000000-0000-0000-0000-000000000005',
     'Carmenere Gran Reserva', 'carmenere-gran-reserva',
     'Valle de Colchagua, Chile. Especiado, frutos negros y taninos sedosos', 48000, 24000, TRUE, FALSE, '/assets/Carmenere.jpg', NULL),
    ('20000000-0000-0000-0000-000000000014', '10000000-0000-0000-0000-000000000005',
     'Sauvignon Blanc', 'sauvignon-blanc',
     'Valle de Casablanca, Chile. Fresco, citrico y mineral', 38000, 19000, TRUE, FALSE, '/assets/Sauvignon_Blanc.jpg', NULL);

INSERT INTO dish_tags (dish_id, tag) VALUES
    ('20000000-0000-0000-0000-000000000001', 'signature'),
    ('20000000-0000-0000-0000-000000000002', 'vegetarian'),
    ('20000000-0000-0000-0000-000000000003', 'signature'),
    ('20000000-0000-0000-0000-000000000005', 'signature'),
    ('20000000-0000-0000-0000-000000000006', 'vegetarian'),
    ('20000000-0000-0000-0000-000000000007', 'spicy'),
    ('20000000-0000-0000-0000-000000000008', 'signature');

INSERT INTO dish_allergens (dish_id, allergen) VALUES
    ('20000000-0000-0000-0000-000000000001', 'pescado'),
    ('20000000-0000-0000-0000-000000000003', 'pescado'),
    ('20000000-0000-0000-0000-000000000004', 'mariscos'),
    ('20000000-0000-0000-0000-000000000004', 'lacteos'),
    ('20000000-0000-0000-0000-000000000006', 'lacteos'),
    ('20000000-0000-0000-0000-000000000007', 'lacteos'),
    ('20000000-0000-0000-0000-000000000007', 'gluten'),
    ('20000000-0000-0000-0000-000000000008', 'lacteos'),
    ('20000000-0000-0000-0000-000000000008', 'huevo'),
    ('20000000-0000-0000-0000-000000000009', 'lacteos'),
    ('20000000-0000-0000-0000-000000000009', 'frutos secos'),
    ('20000000-0000-0000-0000-000000000011', 'huevo');

-- Maridajes sugeridos
INSERT INTO dish_pairings (dish_id, paired_dish_id) VALUES
    ('20000000-0000-0000-0000-000000000001', '20000000-0000-0000-0000-000000000014'),
    ('20000000-0000-0000-0000-000000000003', '20000000-0000-0000-0000-000000000014'),
    ('20000000-0000-0000-0000-000000000005', '20000000-0000-0000-0000-000000000012'),
    ('20000000-0000-0000-0000-000000000006', '20000000-0000-0000-0000-000000000013'),
    ('20000000-0000-0000-0000-000000000007', '20000000-0000-0000-0000-000000000013');

-- Precio inicial en el historial
INSERT INTO dish_price_history (id, dish_id, price)
SELECT gen_random_uuid(), id, price FROM dishes;
