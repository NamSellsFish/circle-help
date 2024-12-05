export default function formatNumber(n: number) {
    if (n) {
        const newNumber = n.toString()

        return newNumber.replace(/\B(?=(\d{3})+(?!\d))/g, ',')
    }
    return ''
}