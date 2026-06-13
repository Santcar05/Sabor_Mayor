INSERT INTO blog_posts (id, title, slug, excerpt, body, cover_image, author_id, author_name,
                        meta_title, meta_description, published, published_at)
VALUES
    ('50000000-0000-0000-0000-000000000001',
     'El renacer de la cocina latinoamericana', 'renacer-cocina-latinoamericana',
     'Un viaje por los sabores ancestrales reinterpretados con tecnica contemporanea.',
     'En Sabor Mayor creemos que la cocina latinoamericana vive un renacimiento...',
     '/assets/chef.jpg', '00000000-0000-0000-0000-000000000002', 'Administrador General',
     'El renacer de la cocina latinoamericana | Sabor Mayor',
     'Descubre como Sabor Mayor reinterpreta los sabores ancestrales de America Latina.',
     TRUE, now()),
    ('50000000-0000-0000-0000-000000000002',
     'Maridajes que cuentan historias', 'maridajes-que-cuentan-historias',
     'Como elegimos cada vino para acompanar nuestros platos insignia.',
     'El maridaje es un dialogo entre el plato y la copa...',
     '/assets/Malbec.jpg', '00000000-0000-0000-0000-000000000002', 'Administrador General',
     'Maridajes que cuentan historias | Sabor Mayor',
     'Guia de maridajes de Sabor Mayor: vinos sudamericanos y alta cocina.',
     TRUE, now());

INSERT INTO gallery_images (id, url, caption, display_order) VALUES
    ('51000000-0000-0000-0000-000000000001', '/assets/ceviche.jpg', 'Ceviche de autor', 1),
    ('51000000-0000-0000-0000-000000000002', '/assets/chef.jpg', 'Nuestro chef ejecutivo', 2),
    ('51000000-0000-0000-0000-000000000003', '/assets/Aji_de_Gallina_Contemporaneo.jpg', 'Aji de gallina contemporaneo', 3);
