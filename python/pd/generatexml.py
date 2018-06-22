import datareader as dr

import xml.etree.ElementTree as ET

marketidname = 'nasdaq'

reader = dr.Datareader()
lists = reader.my(2, 90)

market = ET.Element('market')
rows = ET.SubElement(market, 'rows')
for list in lists:
    symbol = list[0]
    frame = list[1]
    ticker = symbol[0]
    myname = symbol[2]
    for framerow in frame.itertuples():
        row = ET.SubElement(rows, 'row')
        marketid = ET.SubElement(row, 'marketid')
        marketid.text = marketidname
        date = ET.SubElement(row, 'date')
        date.text = framerow[0]
        #.strftime('%Y-%M-%d')
        #date.text = str(framerow[5])
        name = ET.SubElement(row, 'id')
        name.text = ticker
        name = ET.SubElement(row, 'name')
        name.text = myname
        price = ET.SubElement(row, 'price')
        price.text = str(framerow[4])
        pricelow = ET.SubElement(row, 'pricelow')
        pricelow.text = str(framerow[3])
        pricehigh = ET.SubElement(row, 'pricehigh')
        pricehigh.text = str(framerow[2])
        volume= ET.SubElement(row, 'volume')
        volume.text = str(framerow[5])
        
ET.dump(market)
tree =  ET.ElementTree(market)
tree.write('bla.xml')
    
#class GenerateXML:

