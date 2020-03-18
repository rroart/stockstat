all:
ifneq ($(MYTFDATAEXTERN),)
ifneq ($(MYTFDATA),)
	rsync -a $$MYTFDATAEXTERN/ $$MYTFDATA
endif
endif

.PHONY: all
