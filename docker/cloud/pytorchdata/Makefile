all:
ifneq ($(MYPTDATAEXTERN),)
ifneq ($(MYPTDATA),)
	rsync -a $$MYPTDATAEXTERN/ $$MYPTDATA
endif
endif

.PHONY: all
