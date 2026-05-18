DELETE FROM worship_template_items
WHERE template_id IN ('template-family', 'template-thanksgiving');

DELETE FROM worship_templates
WHERE id IN ('template-family', 'template-thanksgiving');

INSERT INTO worship_templates (
    id,
    tenant_id,
    sector_id,
    title,
    description
) VALUES (
    'template-sector',
    'hkbp',
    'sector-1',
    'Partangiangan Sektor/Wijk',
    'Susunan umum partangiangan rutin sektor atau wijk.'
) ON CONFLICT(id) DO UPDATE SET
    tenant_id = excluded.tenant_id,
    sector_id = excluded.sector_id,
    title = excluded.title,
    description = excluded.description,
    updated_at = CURRENT_TIMESTAMP;

DELETE FROM worship_template_items
WHERE template_id = 'template-sector';

INSERT INTO worship_template_items (
    id,
    template_id,
    order_index,
    title,
    content,
    leader,
    type,
    scripture_reference,
    scripture_text,
    note
) VALUES
(
    'template-sector-item-0',
    'template-sector',
    0,
    'Pembukaan',
    'Pelayan membuka ibadah dan mengajak jemaat memusatkan hati kepada Tuhan.',
    'Liturgis',
    'OPENING',
    '',
    '',
    ''
),
(
    'template-sector-item-1',
    'template-sector',
    1,
    'Nyanyian Pembuka',
    'Pilih satu nyanyian pembuka yang sesuai dengan tema persekutuan.',
    'Song Leader',
    'SONG',
    '',
    '',
    ''
),
(
    'template-sector-item-2',
    'template-sector',
    2,
    'Doa Pembuka',
    'Doa singkat untuk menyerahkan persekutuan, keluarga tuan rumah, dan seluruh warga sektor.',
    'Liturgis',
    'PRAYER',
    '',
    '',
    ''
),
(
    'template-sector-item-3',
    'template-sector',
    3,
    'Pembacaan Alkitab',
    'Bacakan nas pilihan, lalu beri jeda singkat untuk perenungan pribadi.',
    'Pembaca Alkitab',
    'SCRIPTURE',
    'Mazmur 100:1-5',
    'Baca nas pilihan sesuai tema partangiangan.',
    ''
),
(
    'template-sector-item-4',
    'template-sector',
    4,
    'Renungan Singkat',
    'Renungan diarahkan pada penguatan iman, kebersamaan sektor, dan pelayanan sehari-hari.',
    'Pembawa Renungan',
    'SERMON',
    '',
    '',
    ''
),
(
    'template-sector-item-5',
    'template-sector',
    5,
    'Doa Syafaat',
    'Doakan keluarga, warga yang sakit, pelayanan gereja, dan pergumulan sektor.',
    'Liturgis',
    'PRAYER',
    '',
    '',
    ''
),
(
    'template-sector-item-6',
    'template-sector',
    6,
    'Persembahan',
    'Jemaat memberi persembahan sebagai ungkapan syukur dan dukungan pelayanan sektor.',
    'Bendahara Sektor',
    'OFFERING',
    '',
    '',
    ''
),
(
    'template-sector-item-7',
    'template-sector',
    7,
    'Pengumuman',
    'Sampaikan agenda sektor, informasi pelayanan, dan kewajiban yang perlu diperhatikan.',
    'Pengurus Sektor',
    'ANNOUNCEMENT',
    '',
    '',
    ''
),
(
    'template-sector-item-8',
    'template-sector',
    8,
    'Doa Penutup',
    'Tutup ibadah dengan doa syukur dan permohonan penyertaan Tuhan.',
    'Liturgis',
    'CLOSING',
    '',
    '',
    ''
);
