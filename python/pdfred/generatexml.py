import datareader as dr

import xml.etree.ElementTree as ET

from datetime import datetime

marketidname = 'fred'

reader = dr.Datareader()
lists = reader.my(2, 90)
market = ET.Element('market')
rows = ET.SubElement(market, 'rows')
for list in lists:
    symbol = list[0]
    frame = list[1]
    ticker = symbol
    myname = symbol
    for framerow in frame.itertuples():
        row = ET.SubElement(rows, 'row')
        marketid = ET.SubElement(row, 'marketid')
        marketid.text = marketidname
        date = ET.SubElement(row, 'date')
        datestr = framerow[0]
        #print(type(datestr))
        #mydate = datetime.strptime(datestr, '%Y-%M-%d')
        mydate = framerow[0].date()
        #print(mydate)
        #print(framerow[0])
        date.text = mydate.strftime('%d.%m.%Y')
        #print(date.text)
        #date.text = str(framerow[5])
        #print(framerow)
        #ET.dump(row)
        name = ET.SubElement(row, 'id')
        name.text = ticker
        name = ET.SubElement(row, 'name')
        name.text = myname
        index = ET.SubElement(row, 'indexvalue')
        index.text = str(framerow[1])
        if index.text == 'nan':
            index.text = '-'
        #indexlow = ET.SubElement(row, 'indexlow')
        #indexlow.text = str(framerow[3])
        #if indexlow.text == 'nan':
        #    indexlow.text = '-'
        #indexhigh = ET.SubElement(row, 'indexhigh')
        #indexhigh.text = str(framerow[2])
        #if indexhigh.text == 'nan':
        #    indexhigh.text = '-'
        #volume = ET.SubElement(row, 'volume')
        #volume.text = str(framerow[5])
        #if volume.text == 'nan':
        #    volume.text = '-'
        
#ET.dump(market)
tree =  ET.ElementTree(market)
tree.write('/tmp/fred.xml')
    
#class GenerateXML:

