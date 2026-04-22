import { useState, useRef, useEffect } from 'react';

/**
 * Responsive graph visualization using SVG with dynamic scaling
 */
function TrafficNetworkGraph({ nodes, edges, source, sink }) {
  const containerRef = useRef(null);
  const [dimensions, setDimensions] = useState({ width: 1000, height: 600 });

  // Monitor container size for responsiveness
  useEffect(() => {
    const resizeObserver = new ResizeObserver(() => {
      if (containerRef.current) {
        const rect = containerRef.current.getBoundingClientRect();
        setDimensions({
          width: Math.max(rect.width, 400),
          height: Math.max(rect.height, 300)
        });
      }
    });

    if (containerRef.current) {
      resizeObserver.observe(containerRef.current);
    }

    return () => resizeObserver.disconnect();
  }, []);

  // Proportional scaling based on actual dimensions
  const padding = 80;
  const nodeRadius = Math.max(25, Math.min(dimensions.width, dimensions.height) * 0.04);
  const labelFontSize = Math.max(14, nodeRadius * 0.8);
  const edgeLabelFontSize = Math.max(12, nodeRadius * 0.6);
  const legendFontSize = Math.max(10, nodeRadius * 0.5);

  // Position nodes in a hierarchical layout with responsive scaling
  const contentWidth = dimensions.width - 2 * padding;
  const contentHeight = dimensions.height - 2 * padding;

  const nodePositions = {
    'A': { x: padding + contentWidth * 0.08, y: padding + contentHeight / 2 },
    'B': { x: padding + contentWidth * 0.25, y: padding + contentHeight * 0.25 },
    'C': { x: padding + contentWidth * 0.25, y: padding + contentHeight * 0.5 },
    'D': { x: padding + contentWidth * 0.25, y: padding + contentHeight * 0.75 },
    'E': { x: padding + contentWidth * 0.5, y: padding + contentHeight * 0.33 },
    'F': { x: padding + contentWidth * 0.5, y: padding + contentHeight * 0.67 },
    'G': { x: padding + contentWidth * 0.75, y: padding + contentHeight * 0.25 },
    'H': { x: padding + contentWidth * 0.75, y: padding + contentHeight * 0.75 },
    'T': { x: padding + contentWidth * 0.92, y: padding + contentHeight / 2 }
  };

  return (
    <div
      ref={containerRef}
      className="traffic-graph-container"
      style={{
        width: '100%',
        height: '100%',
        minHeight: '300px',
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center'
      }}
    >
      <svg
        className="traffic-graph"
        viewBox={`0 0 ${dimensions.width} ${dimensions.height}`}
        preserveAspectRatio="xMidYMid meet"
        style={{
          width: '100%',
          height: '100%',
          maxWidth: '100%',
          maxHeight: '100%'
        }}
      >
        {/* Background */}
        <rect
          width={dimensions.width}
          height={dimensions.height}
          fill="rgba(15, 23, 42, 0.3)"
          rx="12"
        />

        {/* Draw edges */}
        {edges.map((edge, idx) => {
          const fromPos = nodePositions[edge.from];
          const toPos = nodePositions[edge.to];
          return (
            <g key={`edge-${idx}`}>
              {/* Edge line with gradient */}
              <line
                x1={fromPos.x}
                y1={fromPos.y}
                x2={toPos.x}
                y2={toPos.y}
                stroke="rgba(100, 116, 139, 0.6)"
                strokeWidth={Math.max(2, nodeRadius * 0.15)}
              />
              {/* Capacity label with background */}
              <rect
                x={(fromPos.x + toPos.x) / 2 - edgeLabelFontSize * 0.8}
                y={(fromPos.y + toPos.y) / 2 - edgeLabelFontSize * 1.2}
                width={edgeLabelFontSize * 1.6}
                height={edgeLabelFontSize * 1.2}
                fill="rgba(15, 23, 42, 0.9)"
                rx="4"
              />
              <text
                x={(fromPos.x + toPos.x) / 2}
                y={(fromPos.y + toPos.y) / 2}
                fontSize={edgeLabelFontSize}
                fill="#fff"
                textAnchor="middle"
                dominantBaseline="middle"
                fontWeight="bold"
                pointerEvents="none"
              >
                {edge.capacity}
              </text>
            </g>
          );
        })}

        {/* Draw nodes */}
        {nodes.map((node) => {
          const pos = nodePositions[node];
          const bgColor =
            node === source
              ? '#10b981'
              : node === sink
                ? '#ef4444'
                : '#3b82f6';

          return (
            <g key={`node-${node}`}>
              {/* Node circle with shadow */}
              <circle
                cx={pos.x}
                cy={pos.y}
                r={nodeRadius}
                fill={bgColor}
                stroke="rgba(255, 255, 255, 0.3)"
                strokeWidth={Math.max(1, nodeRadius * 0.1)}
                filter="drop-shadow(0 4px 6px rgba(0, 0, 0, 0.4))"
              />
              {/* Node label */}
              <text
                x={pos.x}
                y={pos.y}
                fontSize={labelFontSize}
                fontWeight="bold"
                fill="white"
                textAnchor="middle"
                dominantBaseline="middle"
                pointerEvents="none"
              >
                {node}
              </text>
            </g>
          );
        })}

        {/* Legend */}
        <g transform={`translate(${padding * 0.8}, ${padding * 0.6})`}>
          <rect
            x="-10"
            y="-8"
            width="220"
            height="50"
            fill="rgba(15, 23, 42, 0.95)"
            rx="6"
            stroke="rgba(148, 163, 184, 0.2)"
            strokeWidth="1"
          />
          <text fontSize={legendFontSize} fontWeight="bold" fill="#94a3b8">
            Legend:
          </text>
          <circle cx={`${nodeRadius * 1.2}`} cy={`${legendFontSize * 2.2}`} r={nodeRadius * 0.6} fill="#10b981" />
          <text x={`${nodeRadius * 2.5}`} y={`${legendFontSize * 2.6}`} fontSize={legendFontSize} fill="#cbd5e1">
            Source
          </text>

          <circle cx={`${nodeRadius * 8}`} cy={`${legendFontSize * 2.2}`} r={nodeRadius * 0.6} fill="#ef4444" />
          <text x={`${nodeRadius * 9.3}`} y={`${legendFontSize * 2.6}`} fontSize={legendFontSize} fill="#cbd5e1">
            Sink
          </text>

          <circle cx={`${nodeRadius * 14}`} cy={`${legendFontSize * 2.2}`} r={nodeRadius * 0.6} fill="#3b82f6" />
          <text x={`${nodeRadius * 15.3}`} y={`${legendFontSize * 2.6}`} fontSize={legendFontSize} fill="#cbd5e1">
            Node
          </text>
        </g>
      </svg>
    </div>
  );
}

export default TrafficNetworkGraph;
