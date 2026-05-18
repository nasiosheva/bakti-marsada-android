INSERT INTO worship_templates (
    id,
    tenant_id,
    sector_id,
    title,
    description
) VALUES (
    'template-batak-partangiangan',
    'hkbp',
    'sector-1',
    'Partangiangan Bahasa Batak',
    'Susunan acara partangiangan lengkap marbahasa Batak, termasuk judul Buku Ende, nomor ende, lirik orisinal, ayat, dan isi ayat.'
) ON CONFLICT(id) DO UPDATE SET
    tenant_id = excluded.tenant_id,
    sector_id = excluded.sector_id,
    title = excluded.title,
    description = excluded.description,
    updated_at = CURRENT_TIMESTAMP;

DELETE FROM worship_template_items
WHERE template_id = 'template-batak-partangiangan';

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
    'template-batak-partangiangan-item-0',
    'template-batak-partangiangan',
    0,
    'Pamukkaon',
    'Horas ma hita saluhutna di bagasan asi ni roha ni Tuhan. Sai patunduk ma rohanta, pikiran, dohot nasa na adong di hita, asa boi hita marsomba tu Debata mardongan roha na marsada. Di bagasan partangiangan on, tapangido ma pangurupion ni Tuhan asa hami dipatogu di bagasan haporseaon, holong, dohot hasadaon.',
    'Liturgis',
    'OPENING',
    '',
    '',
    ''
),
(
    'template-batak-partangiangan-item-1',
    'template-batak-partangiangan',
    1,
    'Buku Ende No. 3 - Puji Hamu Jahowa',
    'Ende pamukkaon: Puji ma Jahowa, Debata na badia. Sai pasangaphon ma goarNa di tongatonga ni hita. Roha nami marsomba, soara nami mangende, pasahathon hamuliaon tu Tuhan na mangalehon hangoluan. Sai jalo ma pujipujian nami, sai unang loas rohami dao sian hataM.',
    'Song Leader',
    'SONG',
    '',
    '',
    'Lirik di template on dibuat orisinal sebagai teks panduan aplikasi; sesuaion dohot Buku Ende resmi molo dipakke di parmingguon.'
),
(
    'template-batak-partangiangan-item-2',
    'template-batak-partangiangan',
    2,
    'Tangiang Pamukka',
    'Ale Tuhan Debata, Ama nami na di banua ginjang, hujurhon hami roha nami tu Ho. Pamasumasu ma partangiangan on, jaga ma hami sian haholomon ni roha, patogu ma hami marhite HataM, jala pasada ma hami di bagasan holong ni Kristus. Sai gabe hamuliaon ma sude ulaon nami tu goarMi.',
    'Liturgis',
    'PRAYER',
    '',
    '',
    ''
),
(
    'template-batak-partangiangan-item-3',
    'template-batak-partangiangan',
    3,
    'Pambacaan Ayat',
    'Tapangihuthon ma hata ni Tuhan sian Psalmen 133:1-3. Hata on pasingothon hita asa mangaramoti hasadaon, ai hasadaon i gabe pasupasu di bagasan keluarga, sektor, dohot huria.',
    'Pembaca Alkitab',
    'SCRIPTURE',
    'Psalmen 133:1-3',
    'Beta ida ma, songon dia denggan dohot sonangna molo angka dongan sahuria marsada roha jala rap mangolu di bagasan dame. Hasadaon i songon miak na hushus na tuat sian ulu tu janggut, jala songon andor ni embun na mangalehon hangoluan tu tano. Di na marsada roha i, Tuhan mangalehon pasupasu dohot hangoluan na so marnamuba.',
    'Teks ayat di template on paraphrase Batak untuk panduan acara; pakke teks Alkitab resmi di pelayanan formal.'
),
(
    'template-batak-partangiangan-item-4',
    'template-batak-partangiangan',
    4,
    'Buku Ende No. 248 - Sai Marsomba Ma Hita',
    'Ende mandok hata: Sai marsomba ma hita tu Tuhan na mangolu. Di bagasan las ni roha, tahutahon ma sangap tu goarNa. HataNa gabe sinondang di dalan nami, holongNa gabe pangapul di ngolu nami. Sai tuntun ma hami, ale Tuhan, asa burju hami di ulaonMi.',
    'Song Leader',
    'SONG',
    '',
    '',
    'Lirik di template on dibuat orisinal sebagai teks panduan aplikasi; sesuaion dohot Buku Ende resmi molo dipakke di parmingguon.'
),
(
    'template-batak-partangiangan-item-5',
    'template-batak-partangiangan',
    5,
    'Renungan Singkat',
    'Renungan dipatupa sian hata Psalmen 133:1-3. Di bagasan hasadaon, Tuhan patuduhon pasupasuNa. Molo keluarga, sektor, dohot huria marsada roha, ulaon pelayanan gabe marsahala, na gale gabe tarurupi, na marsahit gabe tarpangapuli, jala na mamorluhon parhatian gabe tarida. Sai unang ma hita holan rap hundul, alai marsada ma hita di bagasan holong, pangampunion, dohot ulaon na denggan.',
    'Pembawa Renungan',
    'SERMON',
    'Psalmen 133:1-3',
    '',
    ''
),
(
    'template-batak-partangiangan-item-6',
    'template-batak-partangiangan',
    6,
    'Tangiang Pangondian',
    'Ale Tuhan, pasangap ma Ho di bagasan ngolu nami. Tangihon ma angka pangidoan nami: pasahat ma hagogoon tu angka na marsahit, pangapul tu na marsak, parbinotoan tu angka na mangulahon pelayanan, jala dame tu angka keluarga di sektor on. Sai lehon ma roha na olo marsipaturean, marsitangiangan, dohot marsihaholongan.',
    'Liturgis',
    'PRAYER',
    '',
    '',
    ''
),
(
    'template-batak-partangiangan-item-7',
    'template-batak-partangiangan',
    7,
    'Buku Ende No. 204 - Pasupasu Ma Ulaon Nami',
    'Ende pasahathon roha: Pasupasu ma ulaon nami, ale Tuhan na burju. Nasa na hupasahat hami, sian tangan dohot sian roha, sai jalo ma i gabe tanda las ni roha. Pake ma hami gabe parhobasMu, mangurupi dongan, mangaramoti huria, jala mangolu di bagasan hataM.',
    'Song Leader',
    'SONG',
    '',
    '',
    'Lirik di template on dibuat orisinal sebagai teks panduan aplikasi; sesuaion dohot Buku Ende resmi molo dipakke di parmingguon.'
),
(
    'template-batak-partangiangan-item-8',
    'template-batak-partangiangan',
    8,
    'Pelean',
    'Tapasahat ma pelean nami tu Tuhan mardongan las ni roha. Sai gabe angka pelean on pangurupion di pelayanan, pangapul tu na ringkot, dohot tanda hamuuli ni roha nami tu Debata naung mangaramoti hita.',
    'Bendahara Sektor',
    'OFFERING',
    '',
    '',
    ''
),
(
    'template-batak-partangiangan-item-9',
    'template-batak-partangiangan',
    9,
    'Paboahon Barita',
    'Dipaboa ma agenda sektor, ulaon parmingguon, jadwal partangiangan na mangihut, informasi pelayanan, dohot angka na ringkot diparrohahon tu ruas.',
    'Pengurus Sektor',
    'ANNOUNCEMENT',
    '',
    '',
    ''
),
(
    'template-batak-partangiangan-item-10',
    'template-batak-partangiangan',
    10,
    'Tangiang Panutup',
    'Ale Tuhan Debata, mauliate ma di sude asi ni rohaM. Tung ingkon Ho do mangiringiring hami mulak tu inganan nami be. Pasada ma keluarga nami, pargogoi ma pelayanan nami, jala lehon ma dame di roha nami. Di bagasan goar ni Tuhan Jesus Kristus, hami martangiang. Amen.',
    'Liturgis',
    'CLOSING',
    '',
    '',
    ''
);
