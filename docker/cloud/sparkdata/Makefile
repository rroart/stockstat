all:
ifneq ($(MYSPARKDATAEXTERN),)
ifneq ($(MYSPARKDATA),)
	rsync -a $$MYSPARKDATAEXTERN/ $$MYSPARKDATA
endif
else
	mkdir -p $$MYSPARKDATA
	touch $$MYSPARKDATA/empty
endif

.PHONY: all
