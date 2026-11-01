# Castle Gates – Slottgrindar för Minecraft (Fabric 1.21.1)

Stora **fällgaller** och **vindbryggor** som dras upp och ner med **kedjor** från en **grindvinsch**.

## Block

| Block | Recept | Vad det gör |
|---|---|---|
| **Fällgaller** | Järngaller i hörnen + 5 järntackor (kors) → 8 st | Tunn järnpanel. Bygg en stor vägg av dem i portöppningen. |
| **Vindbrygga** | 7 plankor + 2 järnklumpar (mitten upptill/nedtill) → 8 st | Träblock. Bygg bron *stående* i portöppningen. |
| **Grindvinsch** | `I C I` / `L R L` / `I I I` (I = järntacka, C = kedja, L = stock, R = redstone) | Drar i kedjorna och öppnar/stänger grinden. |

## Så bygger du en grind

1. Bygg grinden i portöppningen (hur bred och hög du vill, max 1024 block och 48 block hög).
2. Sätt **vinschen ovanför grinden**, i samma lodräta linje som något block i grindens översta rad.
3. Häng vanliga **kedjor** (minecraft:chain) rakt ner från vinschen till grinden. (Det går också att sätta vinschen direkt ovanpå grinden utan kedjor.)
4. **Högerklicka** på vinschen för att öppna/stänga, eller koppla **redstone** till den (signal = öppen, ingen signal = stängd). En spak fungerar perfekt.

### Fällgaller
- Gallret dras uppåt ett block i taget, och kedjorna lindas upp i vinschen.
- Lämna gärna ett **tomt utrymme ovanför porten** (ett vaktrum) – där syns gallret när det dras upp. Om det är massiv mur ovanför ser det ut som att gallret glider in i muren.
- Placera gallerblocken medan du tittar rakt mot porten, så vänds de rätt.

### Vindbrygga
- Placera vindbryggsblocken **medan du tittar åt det håll bron ska fällas ner** (t.ex. stå inne i borgen och titta ut mot vallgraven).
- Bron fälls ner kring sin nedersta rad i tre steg tills den ligger platt. Kedjorna släpps ut när bron är nere och kommer tillbaka när den dras upp.
- Se till att det är luft framför porten där bron ska landa.

## Bygga .jar-filen

Du behöver **Java 21** (t.ex. [Adoptium Temurin 21](https://adoptium.net/)).

**Windows:** öppna mappen i Utforskaren, skriv `cmd` i adressfältet och tryck Enter, kör sedan:
```
gradlew.bat build
```
**Mac/Linux:**
```
./gradlew build
```
Första gången tar det några minuter (Minecraft och Fabric laddas ner). Färdig mod: `build/libs/castlegates-1.0.0.jar`.

**Alternativ utan att installera Java:** ladda upp mappen till ett GitHub-repo. Fliken *Actions* bygger modden automatiskt, och jar-filen finns under *Artifacts* när bygget är klart.

## Installera
1. Installera [Fabric Loader](https://fabricmc.net/use/) för Minecraft 1.21.1.
2. Lägg `castlegates-1.0.0.jar` **och** [Fabric API](https://modrinth.com/mod/fabric-api) (för 1.21.1) i mappen `.minecraft/mods`.
3. Starta Minecraft med Fabric-profilen.

Testa i spelet direkt: `/give @s castlegates:winch`, `/give @s castlegates:portcullis 64`, `/give @s castlegates:drawbridge 64`.
