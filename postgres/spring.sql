alter sequence abovebelow_seq rename to abovebelow_dbid_seq;
alter sequence actioncomponent_seq rename to actioncomponent_dbid_seq;
alter sequence config_seq rename to config_dbid_seq;
alter sequence cont_seq rename to cont_dbid_seq;
alter sequence incdec_seq rename to incdec_dbid_seq;
alter sequence memory_seq rename to memory_id_seq;
alter sequence mlmetrics_seq rename to mlmetrics_dbid_seq;
alter sequence relation_seq rename to relation_dbid_seq;
alter sequence sim_seq rename to sim_dbid_seq;
alter sequence timing_seq rename to timing_dbid_seq;
alter sequence timingbl_seq rename to timingbl_dbid_seq;
ALTER TABLE ONLY abovebelow ALTER COLUMN dbid SET DEFAULT nextval('abovebelow_dbid_seq'::regclass);
ALTER TABLE ONLY actioncomponent ALTER COLUMN dbid SET DEFAULT nextval('actioncomponent_dbid_seq'::regclass);
ALTER TABLE ONLY config ALTER COLUMN dbid SET DEFAULT nextval('config_dbid_seq'::regclass);
ALTER TABLE ONLY cont ALTER COLUMN dbid SET DEFAULT nextval('cont_dbid_seq'::regclass);
ALTER TABLE ONLY incdec ALTER COLUMN dbid SET DEFAULT nextval('incdec_dbid_seq'::regclass);
ALTER TABLE ONLY memory ALTER COLUMN id SET DEFAULT nextval('memory_id_seq'::regclass);
ALTER TABLE ONLY mlmetrics ALTER COLUMN dbid SET DEFAULT nextval('mlmetrics_dbid_seq'::regclass);
ALTER TABLE ONLY relation ALTER COLUMN dbid SET DEFAULT nextval('relation_dbid_seq'::regclass);
ALTER TABLE ONLY sim ALTER COLUMN dbid SET DEFAULT nextval('sim_dbid_seq'::regclass);
ALTER TABLE ONLY timing ALTER COLUMN dbid SET DEFAULT nextval('timing_dbid_seq'::regclass);
ALTER TABLE ONLY timingbl ALTER COLUMN dbid SET DEFAULT nextval('timingbl_dbid_seq'::regclass);