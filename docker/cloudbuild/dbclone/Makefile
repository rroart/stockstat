all:
ifneq ($(MYDUMPEXTERN),)
ifneq ($(MYDUMP),)
	rsync -a $$MYDUMPEXTERN $$MYDUMP
endif
endif

.PHONY: all
