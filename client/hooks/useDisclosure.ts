import { useState } from 'react'

export default function useDisclosure() {
    // -999 is close, -111 is open a single item, positive number is open an item in a collection
    const [isOpen, setIsOpen] = useState(-999)


    const open = (idx?: number) => {
        isOpen === -999 && typeof idx === 'undefined' ? setIsOpen(-111) : setIsOpen(idx as number);
    }

    const close = () => {
        setIsOpen(-999)
    }

    const toggle = () => {
        isOpen === -999 ? open() : close()
    }


    return [isOpen, { open, close, toggle }]
}
