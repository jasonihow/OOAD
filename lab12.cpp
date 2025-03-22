#include <iostream>
using namespace std;

class A
{
public:
    A() { cout << "A constructor called" << endl; }
};

A arrayA[10]; // ✅ 正確，有預設建構子

class B
{
public:
    B() { cout << "B constructor called" << endl; }
};

B arrayB[10]; // ✅ 正確，有預設建構子

class C
{
public:
    C(int x) { cout << "C constructor called with " << x << endl; }
};

C arrayC[3]{C(1), C(2), C(3)}; // ❌ 這行會編譯錯誤，因為 C 沒有預設建構子！

int main()
{
    cout << "Program started" << endl;
    return 0;
}
