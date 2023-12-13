rm -r measurements/4/
rm -r measurements/8/
rm -r measurements/16/

echo "" > measurements/pdp.txt
echo "" > measurements/pip.txt
artillery run artillery-tests/4.yml
mkdir measurements/4/
cp measurements/pdp.txt measurements/4/pdp-4.txt
cp measurements/pip.txt measurements/4/pip-4.txt


echo "" > measurements/pdp.txt
echo "" > measurements/pip.txt
artillery run artillery-tests/8.yml
mkdir measurements/8/
cp measurements/pdp.txt measurements/8/pdp-8.txt
cp measurements/pip.txt measurements/8/pip-8.txt

echo "" > measurements/pdp.txt
echo "" > measurements/pip.txt
artillery run artillery-tests/16.yml
mkdir measurements/16/
cp measurements/pdp.txt measurements/16/pdp-16.txt
cp measurements/pip.txt measurements/16/pip-16.txt




