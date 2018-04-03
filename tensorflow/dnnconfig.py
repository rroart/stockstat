class DNNConfig:
    def __init__(self, steps):
        vars(self).update(steps)
    def __init__(self, hiddenlayers):
        vars(self).update(hiddenlayers)
    def __init__(self, hiddenunits):
        vars(self).update(hiddenunits)
    
