# Projection Mathematics & Viewport Normalization

## Selected Projection: Natural Earth (`+proj=natearth +lon_0=0`)
Formulated by Bojan Šavrič, Tom Patterson, and Bernhard Jenny (2011), the Natural Earth projection is a pseudocylindrical projection using 5th-order polynomials for both parallels and meridians.

### Forward Equations
Given latitude $\phi \in [-56^\circ, 84^\circ]$ and longitude $\lambda \in [-180^\circ, 180^\circ]$ in radians:
$$x = R \cdot \lambda \cdot (A_0 + A_1 \phi^2 + A_2 \phi^4 + \phi^6 (A_3 + A_4 \phi^2))$$
$$y = R \cdot \phi \cdot (B_0 + B_1 \phi^2 + \phi^4 (B_2 + B_3 \phi^2 + B_4 \phi^4))$$
where $R = 6378137.0\text{ m}$ and:
- $A_0 = 0.870700, A_1 = -0.131979, A_2 = -0.013791, A_3 = 0.003971, A_4 = -0.001529$
- $B_0 = 1.007226, B_1 = 0.015085, B_2 = -0.044475, B_3 = 0.028874, B_4 = -0.005916$

### Isotropic ViewBox Normalization (`1200 × 600`)
To guarantee zero aspect-ratio distortion, a uniform isotropic scale $s$ centers the projected bounding box inside `1200 × 600` with `24px` minimum margins:
$$\text{mapX} = \frac{\text{contentLeft} + (x - X_{\min}) \cdot s}{1200.0}, \qquad \text{mapY} = \frac{\text{contentTop} + (Y_{\max} - y) \cdot s}{600.0}$$
