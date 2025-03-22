#include <iostream>
using namespace std;

class Pet
{
public:
    Pet() : weight(1), food("Pet Chow") {}
    ~Pet() {}
    void setWeight(int w) { weight = w; }
    int getWeight() { return weight; }

    void setFood(string f) { food = f; }
    string getFood() { return food; }

    void eat();
    void speak();

protected:
    int weight;
    string food;
};
void Pet::eat()
{
    cout << "eat " << food << endl;
}
void Pet::speak()
{
    cout << "Growl " << endl;
}
int main()
{
    Pet dog;
    dog.setFood("oishi");
    dog.setWeight(20);

    dog.eat();
    dog.speak();

    return 0;
}
