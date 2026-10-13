# Beacons Reforged (CereWare)

Mod Fabric para Minecraft 26.3 que hace los beacons **portables** y, en modo full, mucho más potentes.
Requiere **Fabric API**. Java 25.

## Cómo se usa
1. Coloca un beacon sobre una pirámide (3x3 a 9x9) y abre su menú.
2. Pulsa **Consumir pirámide** (y confirma): la pirámide se convierte en tierra y el beacon guarda su nivel.
3. Rompe el beacon y llévatelo. **Clic derecho al aire** con él en la mano para elegir poderes.
4. Pulsa **C** y mete el beacon en su hueco, o haz **Shift + clic** sobre él en tu inventario (E): sus poderes se aplican mientras lo llevas.
5. Cambio rápido: con otro beacon en la mano, **agáchate + clic derecho al aire** y se intercambia con el equipado.

## Modos (`config/beaconsreforged.json` → `mode`)
- **vanilla**: el beacon funciona como el normal (primario + secundario), solo que portable.
- **full** (por defecto): el tamaño de la pirámide desbloquea poderes, su material da puntos
  (hierro 2 · oro 3 · esmeralda 4 · diamante 5 · netherita 8) y el pago sube el nivel máximo (I-V) de forma permanente.
  Poderes extra: Suerte/Luck (más Fortuna y Botín), Alcance y una Prisa que no acelera la animación.

## Config
`mode`, `consumedBlock`, `showConsumeConfirmation`, `keepBeaconOnDeath`, `luckTotalCap`,
`hasteMiningMultipliers`, `maxLevelPerEffect`. El servidor envía su config a los clientes al entrar.

## Licencia
LGPL-3.0-only (ver `LICENSE` y `COPYING`). © CereWare
