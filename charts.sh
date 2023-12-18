rm -r measurements/4/
rm -r measurements/8/
rm -r measurements/16/
rm -r measurements/32/
rm -r measurements/64/

echo "" > measurements/pdp.txt
echo "" > measurements/pip.txt
artillery run artillery-tests/4.yml
mkdir measurements/4/
cp measurements/pdp.txt measurements/4/pdp-4.txt
# remove the first three lines of the file
sed -i '1,3d' measurements/4/pdp-4.txt
cp measurements/pip.txt measurements/4/pip-4.txt
sed -i '1,3d' measurements/4/pip-4.txt


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

echo "" > measurements/pdp.txt
echo "" > measurements/pip.txt
artillery run artillery-tests/32.yml
mkdir measurements/32/
cp measurements/pdp.txt measurements/32/pdp-32.txt
cp measurements/pip.txt measurements/32/pip-32.txt

echo "" > measurements/pdp.txt
echo "" > measurements/pip.txt
artillery run artillery-tests/64.yml
mkdir measurements/64/
cp measurements/pdp.txt measurements/64/pdp-64.txt
cp measurements/pip.txt measurements/64/pip-64.txt



