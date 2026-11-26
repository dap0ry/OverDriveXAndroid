package com.example.overdrivexandroid.feature_characters.domain

data class Character(
    val id: String,
    val name: String,
    val icon: String,
    val image: String
)

data class GameRoster(
    val gameId: String,
    val gameName: String,
    val logo: String,
    val bgImage: String,
    val chars: List<Character>
)

data class UpdateAvatarRequest(
    val avatar_url: String,
    val full_char_url: String
)

data class UpdateAvatarResponse(
    val msg: String? = null
)

// The same ROSTERS constant from the Web version
val WEB_ROSTERS = listOf(
    GameRoster(
        gameId = "ggst",
        gameName = "Guilty Gear Strive",
        logo = "https://res.cloudinary.com/dydqye3n1/image/upload/f_auto,q_auto/GuiltyGearStriveLogo.png",
        bgImage = "https://res.cloudinary.com/dydqye3n1/image/upload/f_auto,q_auto/guiltyGearWallpaper.jpg",
        chars = listOf(
            Character("sol", "Sol Badguy", "https://res.cloudinary.com/dydqye3n1/image/upload/f_auto,q_auto/SolGuiltyGearIcon.png", "https://res.cloudinary.com/dydqye3n1/image/upload/f_auto,q_auto/SolGuiltyGear.png"),
            Character("ky", "Ky Kiske", "https://res.cloudinary.com/dydqye3n1/image/upload/f_auto,q_auto/KiGuiltyGearIcon.png", "https://res.cloudinary.com/dydqye3n1/image/upload/f_auto,q_auto/KiGuiltyGear.png"),
            Character("may", "May", "https://res.cloudinary.com/dydqye3n1/image/upload/f_auto,q_auto/MayGuiltyGearIcon.png", "https://res.cloudinary.com/dydqye3n1/image/upload/f_auto,q_auto/MayGuiltyGear.png"),
            Character("aba", "A.B.A", "https://res.cloudinary.com/dydqye3n1/image/upload/f_auto,q_auto/AbaGuiltyGearIcon.png", "https://res.cloudinary.com/dydqye3n1/image/upload/f_auto,q_auto/AbaGuiltyGear.png"),
            Character("anji", "Anji Mito", "https://res.cloudinary.com/dydqye3n1/image/upload/f_auto,q_auto/AnjiGuiltyGearIcon.png", "https://res.cloudinary.com/dydqye3n1/image/upload/f_auto,q_auto/AnjiGuiltyGear.png"),
            Character("asuka", "Asuka R#", "https://res.cloudinary.com/dydqye3n1/image/upload/f_auto,q_auto/AsukaGuiltyGearIcon.png", "https://res.cloudinary.com/dydqye3n1/image/upload/f_auto,q_auto/AsukaGuiltyGear.png"),
            Character("axl", "Axl Low", "https://res.cloudinary.com/dydqye3n1/image/upload/f_auto,q_auto/AxelGuiltyGearIcon.png", "https://res.cloudinary.com/dydqye3n1/image/upload/f_auto,q_auto/AxelGuiltyGear.png"),
            Character("baiken", "Baiken", "https://res.cloudinary.com/dydqye3n1/image/upload/f_auto,q_auto/BaikenGuiltyGearIcon.png", "https://res.cloudinary.com/dydqye3n1/image/upload/f_auto,q_auto/BaikenGuiltyGear.png"),
            Character("bedman", "Bedman?", "https://res.cloudinary.com/dydqye3n1/image/upload/f_auto,q_auto/BedManGuiltyGearIcon.png", "https://res.cloudinary.com/dydqye3n1/image/upload/f_auto,q_auto/BedManGuiltyGear.png"),
            Character("bridget", "Bridget", "https://res.cloudinary.com/dydqye3n1/image/upload/f_auto,q_auto/BridgetGuiltyGearIcon.png", "https://res.cloudinary.com/dydqye3n1/image/upload/f_auto,q_auto/BridgetGuiltyGear.png"),
            Character("chaos", "Happy Chaos", "https://res.cloudinary.com/dydqye3n1/image/upload/f_auto,q_auto/ChaosGuiltyGearIcon.png", "https://res.cloudinary.com/dydqye3n1/image/upload/f_auto,q_auto/ChaosGuiltyGear.png"),
            Character("chipp", "Chipp Zanuff", "https://res.cloudinary.com/dydqye3n1/image/upload/f_auto,q_auto/ChipGuiltyGearIcon.png", "https://res.cloudinary.com/dydqye3n1/image/upload/f_auto,q_auto/ChipGuiltyGear.png"),
            Character("elphet", "Elphelt Valentine", "https://res.cloudinary.com/dydqye3n1/image/upload/f_auto,q_auto/ElphetGuiltyGearIcon.png", "https://res.cloudinary.com/dydqye3n1/image/upload/f_auto,q_auto/ElphetGuiltyGear.png"),
            Character("faust", "Faust", "https://res.cloudinary.com/dydqye3n1/image/upload/f_auto,q_auto/FaustGuiltyGearIcon.png", "https://res.cloudinary.com/dydqye3n1/image/upload/f_auto,q_auto/FaustGuiltyGear.png"),
            Character("giovanna", "Giovanna", "https://res.cloudinary.com/dydqye3n1/image/upload/f_auto,q_auto/GiovanaGuiltyGearIcon.png", "https://res.cloudinary.com/dydqye3n1/image/upload/f_auto,q_auto/GiovanaGuiltyGear.png"),
            Character("ino", "I-No", "https://res.cloudinary.com/dydqye3n1/image/upload/f_auto,q_auto/InoGuiltyGearIcon.png", "https://res.cloudinary.com/dydqye3n1/image/upload/f_auto,q_auto/InoGuiltyGear.png"),
            Character("jacko", "Jack-O", "https://res.cloudinary.com/dydqye3n1/image/upload/f_auto,q_auto/JackOGuiltyGearIcon.png", "https://res.cloudinary.com/dydqye3n1/image/upload/f_auto,q_auto/JackOGuiltyGear.png"),
            Character("johnny", "Johnny", "https://res.cloudinary.com/dydqye3n1/image/upload/f_auto,q_auto/JonnyGuiltyGearIcon.png", "https://res.cloudinary.com/dydqye3n1/image/upload/f_auto,q_auto/JonnyGuiltyGear.png"),
            Character("leo", "Leo Whitefang", "https://res.cloudinary.com/dydqye3n1/image/upload/f_auto,q_auto/LeoGuiltyGearIcon.png", "https://res.cloudinary.com/dydqye3n1/image/upload/f_auto,q_auto/LeoGuiltyGear.png"),
            Character("lewis", "Goldlewis Dickinson", "https://res.cloudinary.com/dydqye3n1/image/upload/f_auto,q_auto/LewisGuiltyGearIcon.png", "https://res.cloudinary.com/dydqye3n1/image/upload/f_auto,q_auto/LewisGuiltyGear.png"),
            Character("millia", "Millia Rage", "https://res.cloudinary.com/dydqye3n1/image/upload/f_auto,q_auto/MilliaGuiltyGearIcon.png", "https://res.cloudinary.com/dydqye3n1/image/upload/f_auto,q_auto/MilliaGuiltyGear.png"),
            Character("nagoriyuki", "Nagoriyuki", "https://res.cloudinary.com/dydqye3n1/image/upload/f_auto,q_auto/NagoriyukiGuiltyGearIcon.png", "https://res.cloudinary.com/dydqye3n1/image/upload/f_auto,q_auto/NagoriyukiGuiltyGear.png"),
            Character("potemkin", "Potemkin", "https://res.cloudinary.com/dydqye3n1/image/upload/f_auto,q_auto/PotenkinGuiltyGearIcon.png", "https://res.cloudinary.com/dydqye3n1/image/upload/f_auto,q_auto/PotenkinGuiltyGear.png"),
            Character("queendizzy", "Queen Dizzy", "https://res.cloudinary.com/dydqye3n1/image/upload/f_auto,q_auto/QueenDizzyGuityGearIcon.png", "https://res.cloudinary.com/dydqye3n1/image/upload/f_auto,q_auto/QueenDizzyGuiltyGear.png"),
            Character("ramlethal", "Ramlethal Valentine", "https://res.cloudinary.com/dydqye3n1/image/upload/f_auto,q_auto/RamlethalGuiltyGearIcon.png", "https://res.cloudinary.com/dydqye3n1/image/upload/f_auto,q_auto/RamlethalGuiltyGear.png"),
            Character("sin", "Sin Kiske", "https://res.cloudinary.com/dydqye3n1/image/upload/f_auto,q_auto/SinGuiltyGearIcon.png", "https://res.cloudinary.com/dydqye3n1/image/upload/f_auto,q_auto/SinGuiltyGear.png"),
            Character("slayer", "Slayer", "https://res.cloudinary.com/dydqye3n1/image/upload/f_auto,q_auto/SlayerGuiltyGearIcon.png", "https://res.cloudinary.com/dydqye3n1/image/upload/f_auto,q_auto/SlayerGuiltyGear.png"),
            Character("testament", "Testament", "https://res.cloudinary.com/dydqye3n1/image/upload/f_auto,q_auto/TestamentGuiltyGearIcon.png", "https://res.cloudinary.com/dydqye3n1/image/upload/f_auto,q_auto/TestamentGuiltyGear.png"),
            Character("zato", "Zato-1", "https://res.cloudinary.com/dydqye3n1/image/upload/f_auto,q_auto/ZatoGuiltyGearIcon.png", "https://res.cloudinary.com/dydqye3n1/image/upload/f_auto,q_auto/ZatoGuiltyGear.png")
        )
    ),
    GameRoster(
        gameId = "sf6",
        gameName = "Street Fighter 6",
        logo = "https://res.cloudinary.com/dydqye3n1/image/upload/f_auto,q_auto/StreetFighterLogo.png",
        bgImage = "https://res.cloudinary.com/dydqye3n1/image/upload/f_auto,q_auto/streetFigfhter6.jpg",
        chars = listOf(
            Character("ryu", "Ryu", "https://placehold.co/100x100?text=Ryu", "https://placehold.co/600x800/555/FFF?text=Ryu+Art"),
            Character("chunli", "Chun-Li", "https://placehold.co/100x100?text=ChunLi", "https://placehold.co/600x800/666/FFF?text=ChunLi+Art"),
            Character("ken", "Ken Masters", "https://placehold.co/100x100?text=Ken", "https://placehold.co/600x800/777/FFF?text=Ken+Art"),
            Character("juri", "Juri Han", "https://placehold.co/100x100?text=Juri", "https://placehold.co/600x800/888/FFF?text=Juri+Art"),
            Character("luke", "Luke", "https://placehold.co/100x100?text=Luke", "https://placehold.co/600x800/999/FFF?text=Luke+Art")
        )
    ),
    GameRoster(
        gameId = "tk8",
        gameName = "Tekken 8",
        logo = "https://res.cloudinary.com/dydqye3n1/image/upload/f_auto,q_auto/Tekken8Logo.png",
        bgImage = "https://res.cloudinary.com/dydqye3n1/image/upload/f_auto,q_auto/tekken8Wallpaper.jpg",
        chars = listOf(
            Character("jin", "Jin Kazama", "https://placehold.co/100x100?text=Jin", "https://placehold.co/600x800/112/FFF?text=Jin+Art"),
            Character("kazuya", "Kazuya Mishima", "https://placehold.co/100x100?text=Kazuya", "https://placehold.co/600x800/223/FFF?text=Kazuya+Art"),
            Character("nina", "Nina Williams", "https://placehold.co/100x100?text=Nina", "https://placehold.co/600x800/334/FFF?text=Nina+Art")
        )
    ),
    GameRoster(
        gameId = "dbfz",
        gameName = "DB FighterZ",
        logo = "https://res.cloudinary.com/dydqye3n1/image/upload/f_auto,q_auto/DragonBallFighterZLogo.png",
        bgImage = "https://res.cloudinary.com/dydqye3n1/image/upload/f_auto,q_auto/fighterZWallpaper.jpg",
        chars = listOf(
            Character("goku", "Son Goku", "https://placehold.co/100x100?text=Goku", "https://placehold.co/600x800/445/FFF?text=Goku+Art"),
            Character("vegeta", "Vegeta", "https://placehold.co/100x100?text=Vegeta", "https://placehold.co/600x800/556/FFF?text=Vegeta+Art"),
            Character("cell", "Perfect Cell", "https://placehold.co/100x100?text=Cell", "https://placehold.co/600x800/667/FFF?text=Cell+Art"),
            Character("frieza", "Frieza", "https://placehold.co/100x100?text=Frieza", "https://placehold.co/600x800/778/FFF?text=Frieza+Art"),
            Character("gohan", "Gohan", "https://placehold.co/100x100?text=Gohan", "https://placehold.co/600x800/889/FFF?text=Gohan+Art")
        )
    ),
    GameRoster(
        gameId = "sg",
        gameName = "Skullgirls",
        logo = "https://res.cloudinary.com/dydqye3n1/image/upload/f_auto,q_auto/SkullgirlsLogo.png",
        bgImage = "https://res.cloudinary.com/dydqye3n1/image/upload/f_auto,q_auto/skullgirlsWallpaper.jpg",
        chars = listOf(
            Character("devil", "Devil", "https://res.cloudinary.com/dydqye3n1/image/upload/f_auto,q_auto/DevilSkullGirlsIcon.png", "https://res.cloudinary.com/dydqye3n1/image/upload/f_auto,q_auto/DevilSkullGirls.png")
        )
    ),
    GameRoster(
        gameId = "doa6",
        gameName = "Dead or Alive 6",
        logo = "https://res.cloudinary.com/dydqye3n1/image/upload/f_auto,q_auto/Doa6Logo.png",
        bgImage = "https://res.cloudinary.com/dydqye3n1/image/upload/f_auto,q_auto/deadOrAlive6Wallpaper.jpg",
        chars = listOf(
            Character("kasumi", "Kasumi", "https://res.cloudinary.com/dydqye3n1/image/upload/f_auto,q_auto/KasumiDoa6Icon.png", "https://res.cloudinary.com/dydqye3n1/image/upload/f_auto,q_auto/KasumiDoa6.png"),
            Character("hayabusa", "Ryu Hayabusa", "https://res.cloudinary.com/dydqye3n1/image/upload/f_auto,q_auto/HayabusaDoa6Icon.png", "https://res.cloudinary.com/dydqye3n1/image/upload/f_auto,q_auto/HayabusaDoa6.png"),
            Character("hayate", "Hayate", "https://res.cloudinary.com/dydqye3n1/image/upload/f_auto,q_auto/HayateDoa6Icon.png", "https://res.cloudinary.com/dydqye3n1/image/upload/f_auto,q_auto/HayateDoa6.png"),
            Character("helena", "Helena Douglas", "https://res.cloudinary.com/dydqye3n1/image/upload/f_auto,q_auto/HelenaDoa6Icon.png", "https://res.cloudinary.com/dydqye3n1/image/upload/f_auto,q_auto/HelenaDoa6.png"),
            Character("honoka", "Honoka", "https://res.cloudinary.com/dydqye3n1/image/upload/f_auto,q_auto/HonokaDoa6Icon.png", "https://res.cloudinary.com/dydqye3n1/image/upload/f_auto,q_auto/HonokaDoa6.png"),
            Character("jann", "Jann Lee", "https://res.cloudinary.com/dydqye3n1/image/upload/f_auto,q_auto/JannDoa6Icon.png", "https://res.cloudinary.com/dydqye3n1/image/upload/f_auto,q_auto/JannDoa6.png"),
            Character("diego", "Diego", "https://res.cloudinary.com/dydqye3n1/image/upload/f_auto,q_auto/DiegoDoa6Icon.png", "https://res.cloudinary.com/dydqye3n1/image/upload/f_auto,q_auto/DiegoDoa6.png"),
            Character("zack", "Zack", "https://res.cloudinary.com/dydqye3n1/image/upload/f_auto,q_auto/ZackDoa6Icon.png", "https://res.cloudinary.com/dydqye3n1/image/upload/f_auto,q_auto/ZackDoa6.png")
        )
    )
)
