package server.circlehelp.repositories

import org.springframework.data.jpa.repository.JpaRepository
import server.circlehelp.entities.ImageSource

interface ImageSourceRepository : JpaRepository<ImageSource, String>