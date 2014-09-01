from java.util import HashMap

mols = request.body
freqs = HashMap()
iter = mols.iterator()
while iter.hasNext():
    mol = iter.next()
    atoms = mol.getPropertyObject('atom_count')
    freq = freqs.get(atoms)
    if freq != None:
        freqs.put(atoms, freq + 1)
    else:
        freqs.put(atoms, 1)

request.body = freqs
