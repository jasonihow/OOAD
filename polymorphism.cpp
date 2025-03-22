#include <iostream>
#include <vector>
using namespace std;

class Shape
{
public:
    virtual void draw() = 0; // 純虛擬函式，讓子類別必須實作
    virtual ~Shape() {}      // 記得加虛擬解構函數，避免記憶體洩漏
};

class Circle : public Shape
{
public:
    void draw() override
    {
        cout << "Drawing a Circle" << endl;
    }
};

class Rectangle : public Shape
{
public:
    void draw() override
    {
        cout << "Drawing a Rectangle" << endl;
    }
};

int main()
{
    vector<Shape *> shapes; // 儲存 Shape* 指標的 vector

    // 存放 Circle 和 Rectangle 子類別的指標
    shapes.push_back(new Circle());
    shapes.push_back(new Rectangle());

    // 透過基類指標呼叫子類別的 draw()（多型發揮作用）
    for (Shape *shape : shapes)
    {
        shape->draw(); // 會分別呼叫 Circle 和 Rectangle 的 draw()
    }

    // 釋放記憶體，避免記憶體洩漏
    for (Shape *shape : shapes)
    {
        delete shape;
    }

    return 0;
}
