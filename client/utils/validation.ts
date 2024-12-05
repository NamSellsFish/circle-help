import * as Yup from 'yup'

export const logInSchema = Yup.object().shape({
    email: Yup.string().required('Email is required').email('Please enter a valid email'),
    password: Yup.string().required('Please enter a password').min(6, 'Password must be at least 6 characters long'),
})

export const importProductSchema = Yup.object().shape({
    sku: Yup.string().required('SKU is required').min(3, 'SKU must be at least 3 characters long'),
    name: Yup.string().required('Product Name is required').min(5, 'Product name must be at least 5 characters long'),
    price: Yup.number().required('Price is required').min(0.25, 'Price must be at least 0.25'),
    wholesalePrice: Yup.number().required('Wholesale Price is required').min(0.5, 'Wholesale Price must be at least 0.5'),
    quantity: Yup.number().required('Quantity is required').min(1, 'Quantity must be at least 1').integer('Quantity must be an integer'),
    expirationDate: Yup.string()
        .required('Expiration Date is required')
        .matches(/^\d{4}-\d{1,2}-\d{1,2}$/, 'Expiration Date must be in the format: "Year-Month-Day". Example: 2022-12-31'),
    categories: Yup.string()
        .required('Categories are required')
        .matches(/^[A-Za-z\s]+(,\s*[A-Za-z\s]+)*$/, 'Categories must be in the format: "Category1, Category2, Category3"'),
    supplier: Yup.string().required('Supplier is required').min(3, 'Supplier name must be at least 3 characters long'),
    packageID: Yup.number().required('Package ID is required').min(1, 'Package ID must be at least 1').integer('Package ID must be an integer'),
    note: Yup.string() //.min(5, 'Note must be at least 5 characters long') ,
})

export const nameSchema = Yup.object().shape({
    username: Yup.string().required('Please enter a person name').min(3, 'Name must be at least 3 characters long'),
})

export const emailSchema = Yup.object().shape({
    email: Yup.string().required('Email is required').email('Please enter a valid email'),
})