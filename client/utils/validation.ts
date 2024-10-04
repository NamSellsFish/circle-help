import * as Yup from 'yup'

export const logInSchema = Yup.object().shape({
    email: Yup.string().required('Email is required').email('Please enter a valid email'),
    password: Yup.string().required('Please enter a password').min(6, 'Password must be at least 6 characters long'),
})