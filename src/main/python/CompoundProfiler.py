from rdkit import Chem
import MySQLdb


db = MySQLdb.connect('localhost', 'lac', 'lac', 'vendordbs')
cursor = db.cursor()
cursor.execute("select * from DRUGBANK_FEB_2014 where cd_molweight > 300 and cd_molweight < 600 limit 100")

dict = {}
while True:
	data = cursor.fetchone()
	if data == None: break
	mol = Chem.MolFromMolBlock(data[1])
	molh = Chem.AddHs(mol)
	atoms = molh.GetNumAtoms()
	if atoms in dict:
		dict[atoms] += 1
	else:
		dict[atoms] = 1

db.close()

for k in dict:
	print str(k) + ' -> ' + str(dict[k])

		
	