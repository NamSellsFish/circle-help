export default function transformKey(key: string): string {
    return key
        .replace(/([A-Z])/g, ' $1') // Add space before each uppercase letter
        .replace(/^./, str => str.toUpperCase()) // Capitalize the first letter of the first word
        .replace(/ .+/g, str => str.toLowerCase()) // Lowercase the rest of the words
        .trim(); // Remove leading/trailing spaces
}