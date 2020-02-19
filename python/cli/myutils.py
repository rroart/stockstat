import myconfig as cf

def fixna(v):
    #print(type(v))
    if cf.nafix == 1:
        return(v.dropna())
    else:
        return (v.interpolate(method='linear'))

