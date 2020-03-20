all:
ifneq ($(MYTFDATAEXTERN),)
ifneq ($(MYTFDATA),)
	rsync -a $$MYTFDATAEXTERN/ $$MYTFDATA
endif
else
	mkdir -p $$MYTFDATA
	touch $$MYTFDATA/empty
endif

.PHONY: all
