export default interface Product {
    sku: string;
    name: string;
    price: number;
    wholesalePrice: number;
    quantity: number;
    expirationDate: string;
    imageUrl: string;
    categories: string[];
}