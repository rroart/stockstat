all:
ifneq ($(MYPTDATAEXTERN),)
ifneq ($(MYPTDATA),)
	rsync -a $$MYPTDATAEXTERN/ $$MYPTDATA
endif
else
	mkdir -p $$MYPTDATA
	touch $$MYPTDATA/empty
endif

.PHONY: all
