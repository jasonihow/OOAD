#include <iostream>
using namespace std;

class A
{
public:
    A() {};
    void foo() { cout << "A::foo" << endl; }
    void bar() { cout << "A::bar" << endl; }
};

class B : public A
{
public:
    B() {};
    void foo() { cout << "B::foo" << endl; }
    void bar() { cout << "B::bar" << endl; }
};

int main()
{
    B b;
    b.foo();
    b.bar();
    return 0;
};
