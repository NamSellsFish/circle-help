export function formatToISODate(dateStr: string) {
    return new Date(dateStr.replace(/(\d+)(st|nd|rd|th)/, '$1')).toISOString().split('T')[0];
}