cd AIRSExecutor
rm -f *.o
rm -f ../communication
rm -f ../communication-debug
gcc -O2 -Dlinux -lpthread communication.c twofish.c -o ../communication
echo "Buiding communication module.................................. OK!"
rm -f *.o
gcc -O2 -Dlinux -DFWSAMDEBUG -lpthread communication.c twofish.c -o ../communication-debug
cd ..
echo "Buiding communication-debug module............................ OK!"
