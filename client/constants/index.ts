import { Cloudinary } from "@cloudinary/url-gen";
export const MOCK_EXPO_PUBLIC_BASE_URL = 'http://localhost:3000'

export const EXPO_PUBLIC_BASE_URL = 'http://host21110776.ddns.net:8001'

export const GEMINI_API_KEY = 'AIzaSyCEur07T0oQ4Nu51zjwOcoCrad_2fG1mb8'

export const EXPO_PUBLIC_AUTH_BASE_URL = 'http://host21110776.ddns.net:8002'

export { default as CHAT_FACE_DATA } from './chatFaceData'

export const CLD_STORAGE = new Cloudinary({
    cloud: {
        cloudName: 'dkirx8mro',
        apiKey: '746461432464179',
        apiSecret: 'DLEnQ7NcbihiYu6HKiJBnPCNhNA'
    },
    url: {
        secure: true
    }
});


export * from './mockApiData'


export const blurDataURL =
    'data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAS4AAACnAgMAAADS5ll0AAAADFBMVEX09PTa2trd3d3n5+fDnyYAAAACOUlEQVRo3u2Xv2vbQBSAnyMy1KXu1CHZU2h2Zyz+I/LO1sVQLx6kLPcn+E/Icm4VMmRRSpRCAx0TiiBzRhfqPUOgUC8d+gv6zlYdaX6vS3kfHnyDPj7dybozgKIoiqIoiqIoiqIo/56nX+RcL3EopYru0b6RysoR04GM64dNSLYvMvM5pjaxv/sSt2g8ptOLssWXRXfWekyuAVqWneVjLLxd0NcNw8z6jh7N2fvVgCl7dFYg2l+VOebJOpj7w3XmOVPmzfXDKCtZsta0fv3xhCXbeF0fMcuaT8N8wpYVa3xaFD3Gc4YA2IAjM6WcDLCEuCFzDJkn2cOkFReeUzadQFx/IGYc2SdJGV1sxWRbDuKb2vSzZNuOyjIcr2Wc1dweUVmG30TKnp+GsmHkJWR7R6HMwUxC1j2isuMebJEpzgxPtjmkMnop7gZZjGwZla1kH3LDW832IJQ5+IyYwHzKK3syCKs5AlrNt7DHle2H1TzskOwddLiy/vIXQJhLiJiy1sHyFxCg3cT3eDKzLqPRLm81aXuqysIZtM27TTqsVGWj0MmUJVVZHCwRTwZ5WZUt37cLnmxcrsr6Aq9tKCZUNr5NTiqZ48mo7IAeV4myeShz0C0lZDMX39BmF/3d4VmyZ86WJ0L7JuxQ2aWU7MWouaOzVnPntHHW4M1Z95XcWQO6Y7lTEGymgmVtnwvKivOrrx9/Vp8rz1rNx4usdjw2rNM2CP4PEJcVDRwoiqIoiqIoiqIoiqL8h/wB6OAranSrxR0AAAAASUVORK5CYII='

export const SORT_OPTIONS = [
    {
        name: '',
        value: ''
    },
    {
        name: 'Sku',
        value: 'sku',
    },
    {
        name: 'Name',
        value: 'name',
    },
    {
        name: 'Price',
        value: 'price',
    },
    {
        name: 'Wholesale Price',
        value: 'wholesalePrice',
    },
    {
        name: 'Quantity',
        value: 'quantity',
    },
    {
        name: 'Expiration Date',
        value: 'expirationDate',
    },
    {
        name: 'Package ID',
        value: 'packageID',
    },
]

// PunchIn, Late, FullAttendance, Overtime, EarlyLeave
// {
//     type: 'PunchIn',
//     currDate: 'Thursday, 12 July 2021',
//     punchInTime: '07:23',
//     punchOutTime: '17:31',
//     totalHours: 9
// },
export const attendanceData = [

    {
        type: 'Overtime',
        currDate: 'Tuesday, 19th November 2024',
        punchInTime: '07:53',
        punchOutTime: '18:27',
        totalHours: 10
    },
    {
        type: 'Late',
        currDate: 'Wednesday, 20th November 2024',
        punchInTime: '08:11',
        punchOutTime: '17:03',
        totalHours: 7.5
    },
    {
        type: 'FullAttendance',
        currDate: 'Thursday, 22nd November 2024',
        punchInTime: '07:58',
        punchOutTime: '17:01',
        totalHours: 8
    },

]