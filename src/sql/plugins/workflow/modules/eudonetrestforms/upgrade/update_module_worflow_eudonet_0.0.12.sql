-- Action relance eudonet
INSERT INTO workflow_action
(id_action, name, description, id_workflow, id_state_before, id_state_after, id_icon, is_automatic, is_mass_action, display_order, is_automatic_reflexive_action)
VALUES(19, 'Relance Envoi Eudonet', 'Renvoie des informations vers Eudonet', 1, 2, 2, 1, 0, 0, 6, 0);

-- Tache liee à l'action
INSERT INTO workflow_task
(id_task, task_type_key, id_action, display_order)
VALUES(34, 'taskExportDemand', 19, 1);

-- Parametres eudonet
INSERT INTO task_create_eudonetforms_cf
(id_task, id_forms, id_table, base_url, subscriber_login, subscriber_password, base_name, user_login, user_password, user_lang, product_name)
VALUES(34, 1, '-1', 'http://eudonet.apps.paris.mdp/EudoAPI', 'TERRASSES', 'eudonet', 'EUDO_TERRASSES_DEV', 'WS_USER', '2k6b1FwL', 'Lang_00', 'TERRASSES_DEV');

-- Mapping champs eudonet
INSERT INTO task_create_eudonet_data_cf
(id_attribut, id_task, order_question, eudonet_key, eudonet_key_table, eudonet_key_table_link, eudonet_default_value, eudonet_prefix, eudonet_unique_field)
VALUES(1001, 34, 'reference', '1401-N° Dossier', '1400-Dossier de demande', '', '', 'DSN', 1);
INSERT INTO task_create_eudonet_data_cf
(id_attribut, id_task, order_question, eudonet_key, eudonet_key_table, eudonet_key_table_link, eudonet_default_value, eudonet_prefix, eudonet_unique_field)
VALUES(1003, 34, 'civilite', '204-Civilité', '200-Représentant', '', '', '', 0);
INSERT INTO task_create_eudonet_data_cf
(id_attribut, id_task, order_question, eudonet_key, eudonet_key_table, eudonet_key_table_link, eudonet_default_value, eudonet_prefix, eudonet_unique_field)
VALUES(1004, 34, 'nom_usage', '201-Nom', '200-Représentant', '', '', '', 0);
INSERT INTO task_create_eudonet_data_cf
(id_attribut, id_task, order_question, eudonet_key, eudonet_key_table, eudonet_key_table_link, eudonet_default_value, eudonet_prefix, eudonet_unique_field)
VALUES(1005, 34, 'prenom_representant', '202-Prénom', '200-Représentant', '', '', '', 0);
INSERT INTO task_create_eudonet_data_cf
(id_attribut, id_task, order_question, eudonet_key, eudonet_key_table, eudonet_key_table_link, eudonet_default_value, eudonet_prefix, eudonet_unique_field)
VALUES(1006, 34, 'telephone', '205-Tel', '200-Représentant', '', '', '', 0);
INSERT INTO task_create_eudonet_data_cf
(id_attribut, id_task, order_question, eudonet_key, eudonet_key_table, eudonet_key_table_link, eudonet_default_value, eudonet_prefix, eudonet_unique_field)
VALUES(1008, 34, 'courriel', '206-Email', '200-Représentant', '', '', '', 1);
INSERT INTO task_create_eudonet_data_cf
(id_attribut, id_task, order_question, eudonet_key, eudonet_key_table, eudonet_key_table_link, eudonet_default_value, eudonet_prefix, eudonet_unique_field)
VALUES(1009, 34, 'enseigne', '301-Nom du Commerce (enseigne)', '300-Commerces', '', '', '', 0);
INSERT INTO task_create_eudonet_data_cf
(id_attribut, id_task, order_question, eudonet_key, eudonet_key_table, eudonet_key_table_link, eudonet_default_value, eudonet_prefix, eudonet_unique_field)
VALUES(1010, 34, 'adresse_commerce', '302-Adresse', '300-Commerces', '', '', '', 0);
INSERT INTO task_create_eudonet_data_cf
(id_attribut, id_task, order_question, eudonet_key, eudonet_key_table, eudonet_key_table_link, eudonet_default_value, eudonet_prefix, eudonet_unique_field)
VALUES(1012, 34, 'descriptionCommerce', '316-Description activité commerciale', '300-Commerces', '', '', '', 0);
INSERT INTO task_create_eudonet_data_cf
(id_attribut, id_task, order_question, eudonet_key, eudonet_key_table, eudonet_key_table_link, eudonet_default_value, eudonet_prefix, eudonet_unique_field)
VALUES(1013, 34, 'siret', '313-SIRET', '300-Commerces', '', '', '', 1);
INSERT INTO task_create_eudonet_data_cf
(id_attribut, id_task, order_question, eudonet_key, eudonet_key_table, eudonet_key_table_link, eudonet_default_value, eudonet_prefix, eudonet_unique_field)
VALUES(1014, 34, 'nomSociete', '311-Nom de la société', '300-Commerces', '', '', '', 0);
INSERT INTO task_create_eudonet_data_cf
(id_attribut, id_task, order_question, eudonet_key, eudonet_key_table, eudonet_key_table_link, eudonet_default_value, eudonet_prefix, eudonet_unique_field)
VALUES(1015, 34, '', '1403-Date de demande', '1400-Dossier de demande', '', 'creation_date', '', 0);
INSERT INTO task_create_eudonet_data_cf
(id_attribut, id_task, order_question, eudonet_key, eudonet_key_table, eudonet_key_table_link, eudonet_default_value, eudonet_prefix, eudonet_unique_field)
VALUES(1016, 34, '', '1404-Type de dossier', '1400-Dossier de demande', '', '236', '', 0);
INSERT INTO task_create_eudonet_data_cf
(id_attribut, id_task, order_question, eudonet_key, eudonet_key_table, eudonet_key_table_link, eudonet_default_value, eudonet_prefix, eudonet_unique_field)
VALUES(1019, 34, 'I1_typeInstallation', '1502-Typologie', '1500-Installations', '', '', '', 0);
INSERT INTO task_create_eudonet_data_cf
(id_attribut, id_task, order_question, eudonet_key, eudonet_key_table, eudonet_key_table_link, eudonet_default_value, eudonet_prefix, eudonet_unique_field)
VALUES(1020, 34, 'I1_cartographie', '1504-Adresse', '1500-Installations', '', '', '', 0);
INSERT INTO task_create_eudonet_data_cf
(id_attribut, id_task, order_question, eudonet_key, eudonet_key_table, eudonet_key_table_link, eudonet_default_value, eudonet_prefix, eudonet_unique_field)
VALUES(1024, 34, 'I1_commentaire_installation', '1594-Commantaires installation', '1500-Installations', '', '', '', 0);
INSERT INTO task_create_eudonet_data_cf
(id_attribut, id_task, order_question, eudonet_key, eudonet_key_table, eudonet_key_table_link, eudonet_default_value, eudonet_prefix, eudonet_unique_field)
VALUES(1025, 34, 'I1_longueur', '1507-Longueur (m)', '1500-Installations', '', '', '', 0);
INSERT INTO task_create_eudonet_data_cf
(id_attribut, id_task, order_question, eudonet_key, eudonet_key_table, eudonet_key_table_link, eudonet_default_value, eudonet_prefix, eudonet_unique_field)
VALUES(1026, 34, 'I1_largeur', '1508-Largeur (m)', '1500-Installations', '', '', '', 0);
INSERT INTO task_create_eudonet_data_cf
(id_attribut, id_task, order_question, eudonet_key, eudonet_key_table, eudonet_key_table_link, eudonet_default_value, eudonet_prefix, eudonet_unique_field)
VALUES(1027, 34, 'I1_dateInstallation', '1505-Date d''installation', '1500-Installations', '', '', '', 0);
INSERT INTO task_create_eudonet_data_cf
(id_attribut, id_task, order_question, eudonet_key, eudonet_key_table, eudonet_key_table_link, eudonet_default_value, eudonet_prefix, eudonet_unique_field)
VALUES(1028, 34, 'I1_dateRetrait', '1506-Date de retrait', '1500-Installations', '', '', '', 0);
INSERT INTO task_create_eudonet_data_cf
(id_attribut, id_task, order_question, eudonet_key, eudonet_key_table, eudonet_key_table_link, eudonet_default_value, eudonet_prefix, eudonet_unique_field)
VALUES(1029, 34, 'I1_nbStationnement', '1513-N° Place de Stationnement', '1500-Installations', '', '', '', 0);
INSERT INTO task_create_eudonet_data_cf
(id_attribut, id_task, order_question, eudonet_key, eudonet_key_table, eudonet_key_table_link, eudonet_default_value, eudonet_prefix, eudonet_unique_field)
VALUES(1030, 34, 'I1_base64_map', '1516-Cartographie', '1500-Installations', '', '', '', 0);
INSERT INTO task_create_eudonet_data_cf
(id_attribut, id_task, order_question, eudonet_key, eudonet_key_table, eudonet_key_table_link, eudonet_default_value, eudonet_prefix, eudonet_unique_field)
VALUES(1032, 34, 'I1_plan_photos', '', '102000-Annexes', '1500-Installations', '', '(Plan d''implantation et photos)', 0);
INSERT INTO task_create_eudonet_data_cf
(id_attribut, id_task, order_question, eudonet_key, eudonet_key_table, eudonet_key_table_link, eudonet_default_value, eudonet_prefix, eudonet_unique_field)
VALUES(1035, 34, 'I1_pj_autorisation', '', '102000-Annexes', '1500-Installations', '', '(Autorisation, déclaration sur l''honneur)', 0);
INSERT INTO task_create_eudonet_data_cf
(id_attribut, id_task, order_question, eudonet_key, eudonet_key_table, eudonet_key_table_link, eudonet_default_value, eudonet_prefix, eudonet_unique_field)
VALUES(1037, 34, 'I1_pj_autorisation2', '', '102000-Annexes', '1500-Installations', '', '(Autorisation, déclaration sur l''honneur)', 0);
INSERT INTO task_create_eudonet_data_cf
(id_attribut, id_task, order_question, eudonet_key, eudonet_key_table, eudonet_key_table_link, eudonet_default_value, eudonet_prefix, eudonet_unique_field)
VALUES(1039, 34, 'pj_plan', '', '102000-Annexes', '1400-Dossier de demande', '', '(Plan)', 0);
INSERT INTO task_create_eudonet_data_cf
(id_attribut, id_task, order_question, eudonet_key, eudonet_key_table, eudonet_key_table_link, eudonet_default_value, eudonet_prefix, eudonet_unique_field)
VALUES(1040, 34, 'pj_planTable', '', '102000-Annexes', '1400-Dossier de demande', '', '(Plan de table)', 0);
INSERT INTO task_create_eudonet_data_cf
(id_attribut, id_task, order_question, eudonet_key, eudonet_key_table, eudonet_key_table_link, eudonet_default_value, eudonet_prefix, eudonet_unique_field)
VALUES(1041, 34, 'pj_description', '', '102000-Annexes', '1400-Dossier de demande', '', '(Description sommaire)', 0);
INSERT INTO task_create_eudonet_data_cf
(id_attribut, id_task, order_question, eudonet_key, eudonet_key_table, eudonet_key_table_link, eudonet_default_value, eudonet_prefix, eudonet_unique_field)
VALUES(1052, 34, '', '401-Nom de l''adresse', '400-Adresses du commerce', '', 'COMMERCE', '', 0);
INSERT INTO task_create_eudonet_data_cf
(id_attribut, id_task, order_question, eudonet_key, eudonet_key_table, eudonet_key_table_link, eudonet_default_value, eudonet_prefix, eudonet_unique_field)
VALUES(1053, 34, 'url_recap', '', '102000-Annexes', '1400-Demandes', '', '', 0);
INSERT INTO task_create_eudonet_data_cf
(id_attribut, id_task, order_question, eudonet_key, eudonet_key_table, eudonet_key_table_link, eudonet_default_value, eudonet_prefix, eudonet_unique_field)
VALUES(1054, 34, 'I1_numInstallation', '1501-N° Installation', '1500-Installations', '', '', 'DSN', 1);
INSERT INTO task_create_eudonet_data_cf
(id_attribut, id_task, order_question, eudonet_key, eudonet_key_table, eudonet_key_table_link, eudonet_default_value, eudonet_prefix, eudonet_unique_field)
VALUES(1055, 34, 'I1_cartographie', '1514-CP', '1500-Installations', '', '', '', 0);
INSERT INTO task_create_eudonet_data_cf
(id_attribut, id_task, order_question, eudonet_key, eudonet_key_table, eudonet_key_table_link, eudonet_default_value, eudonet_prefix, eudonet_unique_field)
VALUES(1056, 34, 'adresse_commerce', '309-CP', '300-Commerces', '', '', '', 0);
INSERT INTO task_create_eudonet_data_cf
(id_attribut, id_task, order_question, eudonet_key, eudonet_key_table, eudonet_key_table_link, eudonet_default_value, eudonet_prefix, eudonet_unique_field)
VALUES(1057, 34, '', '1410-Décision', '1400-Dossier de demande', '', '', '', 0);
INSERT INTO task_create_eudonet_data_cf
(id_attribut, id_task, order_question, eudonet_key, eudonet_key_table, eudonet_key_table_link, eudonet_default_value, eudonet_prefix, eudonet_unique_field)
VALUES(1058, 34, '', '1408-Motifs de refus', '1400-Dossier de demande', '', '', '', 0);

-- ManagewError mapping
INSERT INTO managewferror_mapping
(id_config, id_wf_relaunch, action_wf_begin, action_wf_relaunch)
VALUES(1, 1, 9, 19);

-- ManagewError config
INSERT INTO managewferror_config
(id_workflow, id_workflow_mapping, id_config, title)
VALUES(1, 1, 1, 'CONFIG FOR EUDONET');
