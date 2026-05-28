import sys

import unittest

import config
import midicli as cli
import time

submodels = ['vq-vae', 'figaro-expert', 'figaro-inst', 'figaro-chord', 'figaro-meta', 'figaro-no-inst', 'figaro-no-chord', 'figaro-no-meta', 'baseline', 'figaro-learned', 'figaro']
submodels = ['vq-vae', 'figaro-learned', 'figaro']

class MyTestCase(unittest.TestCase):
    def test_something(self):
        #return
        testmap = { }
        testmap [ config.PYTORCHGPTMIDIRPR ] = ['maestro']
        testmap [ config.PYTORCHGPTMIDI ] = ['maestro']
        testmap [ config.PYTORCHGPTMIDIFIGARO ] = ['lmd_full']
        testmap [ config.PYTORCHGPTMIDIMMT] = [ 'sod', 'lmd_full', 'snd' ]
        testmap [ config.PYTORCHGPTMIDIMMT] = [ 'lmd_full', 'snd' ]
        #testmap [ config.PYTORCHGPTMIDIMMT] = [ 'snd' ]
        testlist = [ config.PYTORCHGPTMIDIRPR, config.PYTORCHGPTMIDIRPR, config.PYTORCHGPTMIDIFIGARO ]
        testlist = [ config.PYTORCHGPTMIDIMMT ]
        testlist = [ config.PYTORCHGPTMIDIFIGARO ]
        testlist = [ config.PYTORCHGPTMIDI, config.PYTORCHGPTMIDIRPR] #, config.PYTORCHGPTMIDIMMT ]
        testlist = [ config.PYTORCHGPTMIDIMMT ]
        if len(sys.argv) > 1 and sys.argv[1] == "figaro":
            testlist = [ config.PYTORCHGPTMIDIFIGARO ]
        loss = {}
        times = {}

        for test in testlist:
          try:
            curtime = time.time()
            ds = ""

            #result = cli.learn(ds = 'maestro', cf = test, take = 40, steps = 1)
            dslist = testmap[test]
            for ds in dslist:
                submodel = None
                if test == config.PYTORCHGPTMIDIFIGARO:
                    #submodel = 'figaro-expert'
                    submodel = 'vq-vae'
                    submodel = 'figaro'
                result = cli.learn(ds = ds, cf = test, take = 40, steps = 1, submodel = submodel)
                print(result)
                #return
                self.assertIsNotNone(result['accuracy'], "Accuracy")  # add assertion
                result = cli.generate(text ="I like travelling", ds = ds, cf = test, take = 40, submodel = submodel)
                print(result)
                #self.assertIsNotNone(result['classifyarray'][0], "Text")  # add assertion
                loss[(test,ds)] = (result['loss'])
                times[(test,ds)] = time.time() - curtime
          except Exception:
                import traceback
                traceback.print_exc()
                pass
        print("loss", loss)
        print("times", times)

    def test_figaro_learn(self):
        return
        test = config.PYTORCHGPTMIDIFIGARO
        dslist = ['lmd_full']

        for ds in dslist:
            #submodel = 'figaro-expert'
            #result = cli.learn(ds=ds, cf=test, take=40, steps=1, submodel=submodel)
            #print(result)
            for submodel in submodels:
                result = cli.learn(ds=ds, cf=test, take=16, steps=1, submodel=submodel)
                print(result)


    def test_figaro_gen(self):
        return
        test = config.PYTORCHGPTMIDIFIGARO
        dslist = ['lmd_full']

        for ds in dslist:
            #submodel = 'figaro-expert'
            #result = cli.learn(ds=ds, cf=test, take=40, steps=1, submodel=submodel)
            #print(result)
            for submodel in submodels:
                if submodel == 'vq-vae':
                    continue
                result = cli.generate(text ="I like travelling", ds = ds, cf = test, take = 64, submodel = submodel)
                print(result)

    def test_figaro_learn_gen(self):
        return
        test = config.PYTORCHGPTMIDIFIGARO
        dslist = ['lmd_full']

        for ds in dslist:
            submodels = ['figaro-expert']
            for submodel in submodels:
                result = cli.learn(ds=ds, cf=test, take=16, steps=1, submodel=submodel)
                print(result)
                result = cli.generate(text ="I like travelling", ds = ds, cf = test, take = 64, submodel = submodel)
                print(result)


if __name__ == '__main__':
    unittest.main()
