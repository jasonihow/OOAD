#include <iostream>
using namespace std;

// 基礎類別 animal
class animal
{
public: // 讓建構子可被繼承的類別使用
    animal() : age(6), weight(20)
    {
        cout << "Animal default constructor called" << endl;
    }
    animal(int n) : age(n), weight(20)
    {
        cout << "Animal constructor with 1 parameter called" << endl;
    }
    animal(int n, int m) : age(n), weight(m)
    {
        cout << "Animal constructor with 2 parameters called" << endl;
    }
    void showInfo()
    {
        cout << "Age: " << age << ", Weight: " << weight << endl;
    }
    virtual void speak() { cout << "Growl " << endl; }

    ~animal() { cout << "Animal destructor called" << endl; }

private:
    int age;
    int weight;
};

// 派生類別 dog
class dog : public animal
{ // ✅ 一定要加 `public`
public:
    dog()
    { // ✅ 明確呼叫 `animal()` 預設建構子
        cout << "Dog default constructor called" << endl;
    }
    dog(int n) : animal(n)
    { // ✅ 傳遞 `n` 給 `animal(int n)`
        cout << "Dog constructor with 1 parameter called" << endl;
    }
    dog(int n, int m) : animal(n, m)
    { // ✅ 傳遞 `n, m` 給 `animal(int n, int m)`
        cout << "Dog constructor with 2 parameters called" << endl;
    }
    dog(int n, int m, int p) : animal(n, m), P(p)
    { // ✅ 傳遞 `n, m` 給 `animal(int n, int m)`
        cout << "Dog constructor with 3 parameters called" << endl;
    }

    void getP() { cout << "P: " << P << endl; }
    virtual void speak() { cout << "Bark " << endl; }

    ~dog() { cout << "Dog destructor called" << endl; }

private:
    int P;
};

int main()
{
    animal animal1;
    dog dog1;
    animal *nose = (animal *)new dog();

    animal1.speak();
    dog1.speak();
    ((animal)dog1).speak(); // copy constructor啟動 把copy的轉成animal型態 之後馬上透過destructor free掉
    nose->speak();
    return 0;
}
